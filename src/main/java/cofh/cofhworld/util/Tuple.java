package cofh.cofhworld.util;

import java.util.Objects;

public class Tuple<A, B> {

	private A a;
	private B b;

	public Tuple(A a, B b) {

		this.a = a;
		this.b = b;
	}

	public A getA() {

		return this.a;
	}

	public B getB() {

		return this.b;
	}

	public int hashCode() {

		return Objects.hashCode(a) ^ (Objects.hashCode(b) * 31);
	}

	public boolean equals(Object o) {

		if (o == this)
			return true;
		if (o instanceof Tuple) {
			Tuple<?, ?> c = (Tuple<?, ?>) o;
			return Objects.equals(a, c.a) && Objects.equals(b, c.b);
		}
		return false;
	}
}
