package com.typesafe.config.impl;

import com.typesafe.config.*;

import javax.annotation.Nullable;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

public class CoFHConfigObject extends AbstractConfigObject implements Serializable {

	private static final long serialVersionUID = 2L;

	// this map should never be modified - assume immutable
	final private Map<String, AbstractConfigValue> value;
	final private boolean resolved;
	final private boolean ignoresFallbacks;

	CoFHConfigObject(ConfigOrigin origin,
			Map<String, AbstractConfigValue> value, ResolveStatus status,
			boolean ignoresFallbacks) {

		super(origin);
		if (value == null)
			throw new ConfigException.BugOrBroken(
					"creating config object with null map");
		this.value = value;
		this.resolved = status == ResolveStatus.RESOLVED;
		this.ignoresFallbacks = ignoresFallbacks;

		// Kind of an expensive debug check. Comment out?
		if (status != ResolveStatus.fromValues(value.values()))
			throw new ConfigException.BugOrBroken("Wrong resolved status on " + this);
	}

	CoFHConfigObject(ConfigOrigin origin,
			Map<String, AbstractConfigValue> value) {

		this(origin, value, ResolveStatus.fromValues(value.values()), false /* ignoresFallbacks */);
	}

	@Override
	public CoFHConfigObject withOnlyKey(String key) {

		return withOnlyPath(Path.newKey(key));
	}

	@Override
	public CoFHConfigObject withoutKey(String key) {

		return withoutPath(Path.newKey(key));
	}

	// gets the object with only the path if the path
	// exists, otherwise null if it doesn't. this ensures
	// that if we have { a : { b : 42 } } and do
	// withOnlyPath("a.b.c") that we don't keep an empty
	// "a" object.
	@Nullable
	@Override
	protected CoFHConfigObject withOnlyPathOrNull(Path path) {

		String key = path.first();
		Path next = path.remainder();
		AbstractConfigValue v = value.get(key);

		if (next != null) {
			if ((v instanceof AbstractConfigObject)) {
				v = ((AbstractConfigObject) v).withOnlyPathOrNull(next);
			} else {
				// if the path has more elements but we don't have an object,
				// then the rest of the path does not exist.
				v = null;
			}
		}

		if (v == null) {
			return null;
		} else {
			return new CoFHConfigObject(origin(), Collections.singletonMap(key, v), v.resolveStatus(), ignoresFallbacks);
		}
	}

	@Override
	CoFHConfigObject withOnlyPath(Path path) {

		CoFHConfigObject o = withOnlyPathOrNull(path);
		if (o == null) {
			return new CoFHConfigObject(origin(), Collections.emptyMap(), ResolveStatus.RESOLVED, ignoresFallbacks);
		} else {
			return o;
		}
	}

	@Override
	CoFHConfigObject withoutPath(Path path) {

		String key = path.first();
		Path next = path.remainder();
		AbstractConfigValue v = value.get(key);

		if (next != null && v instanceof AbstractConfigObject) {
			v = ((AbstractConfigObject) v).withoutPath(next);
			Map<String, AbstractConfigValue> updated = new LinkedHashMap<>(value);
			updated.put(key, v);
			return new CoFHConfigObject(origin(), updated, ResolveStatus.fromValues(updated.values()), ignoresFallbacks);
		} else if (next != null || v == null) {
			// can't descend, nothing to remove
			return this;
		} else {
			Map<String, AbstractConfigValue> smaller = new LinkedHashMap<>(value.size() - 1);
			for (Map.Entry<String, AbstractConfigValue> old : value.entrySet()) {
				if (!old.getKey().equals(key))
					smaller.put(old.getKey(), old.getValue());
			}
			return new CoFHConfigObject(origin(), smaller, ResolveStatus.fromValues(smaller.values()), ignoresFallbacks);
		}
	}

	@Override
	public CoFHConfigObject withValue(String key, ConfigValue v) {

		if (v == null)
			throw new ConfigException.BugOrBroken(
					"Trying to store null ConfigValue in a ConfigObject");

		Map<String, AbstractConfigValue> newMap;
		if (value.isEmpty()) {
			newMap = Collections.singletonMap(key, (AbstractConfigValue) v);
		} else {
			newMap = new LinkedHashMap<>(value);
			newMap.put(key, (AbstractConfigValue) v);
		}

		return new CoFHConfigObject(origin(), newMap, ResolveStatus.fromValues(newMap.values()), ignoresFallbacks);
	}

	@Override
	CoFHConfigObject withValue(Path path, ConfigValue v) {

		String key = path.first();
		Path next = path.remainder();

		if (next == null) {
			return withValue(key, v);
		} else {
			AbstractConfigValue child = value.get(key);
			if (child instanceof AbstractConfigObject) {
				// if we have an object, add to it
				return withValue(key, ((AbstractConfigObject) child).withValue(next, v));
			} else {
				// as soon as we have a non-object, replace it entirely
				SimpleConfig subtree = ((AbstractConfigValue) v).atPath(SimpleConfigOrigin.newSimple("withValue(" + next.render() + ")"), next);
				return withValue(key, subtree.root());
			}
		}
	}

	@Override
	protected AbstractConfigValue attemptPeekWithPartialResolve(String key) {

		return value.get(key);
	}

	private CoFHConfigObject newCopy(ResolveStatus newStatus, ConfigOrigin newOrigin,
			boolean newIgnoresFallbacks) {

		return new CoFHConfigObject(newOrigin, value, newStatus, newIgnoresFallbacks);
	}

	@Override
	protected CoFHConfigObject newCopy(ResolveStatus newStatus, ConfigOrigin newOrigin) {

		return newCopy(newStatus, newOrigin, ignoresFallbacks);
	}

	@Override
	protected CoFHConfigObject withFallbacksIgnored() {

		if (ignoresFallbacks)
			return this;
		else
			return newCopy(resolveStatus(), origin(), true /* ignoresFallbacks */);
	}

	@Override
	ResolveStatus resolveStatus() {

		return ResolveStatus.fromBoolean(resolved);
	}

	@Override
	protected boolean ignoresFallbacks() {

		return ignoresFallbacks;
	}

	@Override
	public Map<String, Object> unwrapped() {

		Map<String, Object> m = new LinkedHashMap<>();
		for (Map.Entry<String, AbstractConfigValue> e : value.entrySet()) {
			m.put(e.getKey(), e.getValue().unwrapped());
		}
		return m;
	}

	@Override
	protected CoFHConfigObject mergedWithObject(AbstractConfigObject abstractFallback) {

		requireNotIgnoringFallbacks();

		if (!(abstractFallback instanceof CoFHConfigObject)) {
			throw new ConfigException.BugOrBroken(
					"should not be reached (merging non-CoFHConfigObject)");
		}

		CoFHConfigObject fallback = (CoFHConfigObject) abstractFallback;

		boolean changed = false;
		boolean allResolved = true;
		Map<String, AbstractConfigValue> merged = new LinkedHashMap<>();
		Set<String> allKeys = new LinkedHashSet<>();
		allKeys.addAll(this.keySet());
		allKeys.addAll(fallback.keySet());
		for (String key : allKeys) {
			AbstractConfigValue first = this.value.get(key);
			AbstractConfigValue second = fallback.value.get(key);
			AbstractConfigValue kept;
			if (first == null)
				kept = second;
			else if (second == null)
				kept = first;
			else
				kept = first.withFallback(second);

			merged.put(key, kept);

			if (first != kept)
				changed = true;

			if (kept.resolveStatus() == ResolveStatus.UNRESOLVED)
				allResolved = false;
		}

		ResolveStatus newResolveStatus = ResolveStatus.fromBoolean(allResolved);
		boolean newIgnoresFallbacks = fallback.ignoresFallbacks();

		if (changed)
			return new CoFHConfigObject(mergeOrigins(this, fallback), merged, newResolveStatus, newIgnoresFallbacks);
		else if (newResolveStatus != resolveStatus() || newIgnoresFallbacks != ignoresFallbacks())
			return newCopy(newResolveStatus, origin(), newIgnoresFallbacks);
		else
			return this;
	}

	private CoFHConfigObject modify(NoExceptionsModifier modifier) {

		try {
			return modifyMayThrow(modifier);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigException.BugOrBroken("unexpected checked exception", e);
		}
	}

	private CoFHConfigObject modifyMayThrow(Modifier modifier) throws Exception {

		Map<String, AbstractConfigValue> changes = null;
		for (String k : keySet()) {
			AbstractConfigValue v = value.get(k);
			// "modified" may be null, which means remove the child;
			// to do that we put null in the "changes" map.
			AbstractConfigValue modified = modifier.modifyChildMayThrow(k, v);
			if (modified != v) {
				if (changes == null)
					changes = new LinkedHashMap<>();
				changes.put(k, modified);
			}
		}
		if (changes == null) {
			return this;
		} else {
			Map<String, AbstractConfigValue> modified = new LinkedHashMap<>();
			boolean sawUnresolved = false;
			for (String k : keySet()) {
				boolean old = !changes.containsKey(k);
				AbstractConfigValue newValue = (!old ? changes : value).get(k);
				if (old || newValue != null) {
					// preserve old functionality: if not modified, always add it back even if null (not sure this is even possible)
					modified.put(k, newValue);
					sawUnresolved |= (newValue.resolveStatus() == ResolveStatus.UNRESOLVED);
				}
			}
			return new CoFHConfigObject(origin(), modified,
					sawUnresolved ? ResolveStatus.UNRESOLVED : ResolveStatus.RESOLVED,
					ignoresFallbacks());
		}
	}

	@Override
	AbstractConfigObject resolveSubstitutions(final ResolveContext context) throws NotPossibleToResolve {

		if (resolveStatus() == ResolveStatus.RESOLVED)
			return this;

		try {
			return modifyMayThrow((key, v) -> {

				if (context.isRestrictedToChild()) {
					if (key.equals(context.restrictToChild().first())) {
						Path remainder = context.restrictToChild().remainder();
						if (remainder != null) {
							return context.restrict(remainder).resolve(v);
						} else {
							// we don't want to resolve the leaf child.
							return v;
						}
					} else {
						// not in the restrictToChild path
						return v;
					}
				} else {
					// no restrictToChild, resolve everything
					return context.unrestricted().resolve(v);
				}
			});
		} catch (NotPossibleToResolve | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigException.BugOrBroken("unexpected checked exception", e);
		}
	}

	@Override
	CoFHConfigObject relativized(final Path prefix) {

		return modify(new NoExceptionsModifier() {

			@Override
			public AbstractConfigValue modifyChild(String key, AbstractConfigValue v) {

				return v.relativized(prefix);
			}

		});
	}

	@Override
	protected void render(StringBuilder sb, int indent, boolean atRoot, ConfigRenderOptions options) {

		if (isEmpty()) {
			sb.append("{}");
		} else {
			boolean outerBraces = options.getJson() || !atRoot;

			int innerIndent;
			if (outerBraces) {
				innerIndent = indent + 1;
				sb.append("{");

				if (options.getFormatted())
					sb.append('\n');
			} else {
				innerIndent = indent;
			}

			int separatorCount = 0;
			String[] keys = keySet().toArray(new String[size()]);
			Arrays.sort(keys);
			for (String k : keys) {
				AbstractConfigValue v;
				v = value.get(k);

				if (options.getOriginComments()) {
					indent(sb, innerIndent, options);
					sb.append("# ");
					sb.append(v.origin().description());
					sb.append("\n");
				}
				if (options.getComments()) {
					for (String comment : v.origin().comments()) {
						indent(sb, innerIndent, options);
						sb.append("#");
						if (!comment.startsWith(" "))
							sb.append(' ');
						sb.append(comment);
						sb.append("\n");
					}
				}
				indent(sb, innerIndent, options);
				v.render(sb, innerIndent, false /* atRoot */, k, options);

				if (options.getFormatted()) {
					if (options.getJson()) {
						sb.append(",");
						separatorCount = 2;
					} else {
						separatorCount = 1;
					}
					sb.append('\n');
				} else {
					sb.append(",");
					separatorCount = 1;
				}
			}
			// chop last commas/newlines
			sb.setLength(sb.length() - separatorCount);

			if (outerBraces) {
				if (options.getFormatted()) {
					sb.append('\n'); // put a newline back
					indent(sb, indent, options);
				}
				sb.append("}");
			}
		}
		if (atRoot && options.getFormatted())
			sb.append('\n');
	}

	@Override
	public AbstractConfigValue get(Object key) {

		return value.get(key);
	}

	private static boolean mapEquals(Map<String, ConfigValue> a, Map<String, ConfigValue> b) {

		Set<String> aKeys = a.keySet();
		Set<String> bKeys = b.keySet();

		if (!aKeys.equals(bKeys))
			return false;

		for (String key : aKeys) {
			if (!a.get(key).equals(b.get(key)))
				return false;
		}
		return true;
	}

	private static int mapHash(Map<String, ConfigValue> m) {
		// the keys have to be sorted, otherwise we could be equal
		// to another map but have a different hashcode.
		List<String> keys = new ArrayList<>(m.keySet());
		Collections.sort(keys);

		int valuesHash = 0;
		for (String k : keys) {
			valuesHash += m.get(k).hashCode();
		}
		return 41 * (41 + keys.hashCode()) + valuesHash;
	}

	@Override
	protected boolean canEqual(Object other) {

		return other instanceof ConfigObject;
	}

	@Override
	public boolean equals(Object other) {
		// note that "origin" is deliberately NOT part of equality.
		// neither are other "extras" like ignoresFallbacks or resolve status.
		if (other instanceof ConfigObject) {
			// optimization to avoid unwrapped() for two ConfigObject,
			// which is what AbstractConfigValue does.
			return canEqual(other) && mapEquals(this, ((ConfigObject) other));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		// note that "origin" is deliberately NOT part of equality
		// neither are other "extras" like ignoresFallbacks or resolve status.
		return mapHash(this);
	}

	@Override
	public boolean containsKey(Object key) {

		return value.containsKey(key);
	}

	@Override
	public Set<String> keySet() {

		return value.keySet();
	}

	@Override
	public boolean containsValue(Object v) {

		return value.containsValue(v);
	}

	@Override
	public Set<Map.Entry<String, ConfigValue>> entrySet() {
		// total bloat just to work around lack of type variance

		LinkedHashSet<java.util.Map.Entry<String, ConfigValue>> entries = new LinkedHashSet<>();
		for (Map.Entry<String, AbstractConfigValue> e : value.entrySet()) {
			entries.add(new AbstractMap.SimpleImmutableEntry<>(
					e.getKey(), e
					.getValue()));
		}
		return entries;
	}

	@Override
	public boolean isEmpty() {

		return value.isEmpty();
	}

	@Override
	public int size() {

		return value.size();
	}

	@Override
	public Collection<ConfigValue> values() {

		return new LinkedHashSet<>(value.values());
	}

	final private static String EMPTY_NAME = "empty config";
	final private static CoFHConfigObject emptyInstance = empty(SimpleConfigOrigin
			.newSimple(EMPTY_NAME));

	static CoFHConfigObject empty() {

		return emptyInstance;
	}

	static CoFHConfigObject empty(ConfigOrigin origin) {

		if (origin == null)
			return empty();
		else
			return new CoFHConfigObject(origin,
					Collections.emptyMap());
	}

	// serialization all goes through SerializedConfigValue
	private Object writeReplace() throws ObjectStreamException {

		return new SerializedConfigValue(this);
	}
}
