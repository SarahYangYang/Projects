package 531_1;

public class Heap {
	int size;
	int length;
	int[] A;
	int[] p;
	int[] key;

	Heap(int n) {
		size = n;
		length = 0;
		A = new int[n + 1];
		A[0] = -1;
		p = new int[n + 1];
		p[0] = -1;
		key = new int[n + 1];
		key[0] = -1;
	}

	void heapify_up(int i) {
		while (i > 1) {
			int j = i / 2;
			if (key[A[i]] < key[A[j]]) {
				int temp = A[i];
				A[i] = A[j];
				A[j] = temp;
				p[A[i]] = i;
				p[A[j]] = j;
				i = j;
			} else {
				break;
			}
		}
	}

	void insert(int v, int key_value) {
		length = length + 1;
		A[length] = v;
		p[v] = length;
		key[v] = key_value;
		heapify_up(length);
	}

	int extract_min() {
		if (length < 1)
			return -1;
		int ret = A[1];
		A[1] = A[length];
		p[A[1]] = 1;
		length = length - 1;
		if (length >= 1) {
			heapify_down(1);
		}
		return ret;
	}

	void heapify_down(int i) {
		while (2 * i <= length) {
			int j;
			if (2 * i == length || key[A[2 * i]] <= key[A[2 * i + 1]]) {
				j = 2 * i;
			} else {
				j = 2 * i + 1;
			}
			if (key[A[j]] < key[A[i]]) {
				int temp = A[i];
				A[i] = A[j];
				A[j] = temp;
				p[A[i]] = i;
				p[A[j]] = j;
				i = j;
			} else {
				break;
			}
		}
	}

	void decrease_key(int v, int key_value) {
		key[v] = key_value;
		heapify_up(p[v]);
	}

	// public static void main(String args[]) {
	// Heap hp = new Heap(5);
	// hp.insert(1, 10);
	// hp.insert(2, 9);
	// hp.insert(3, 7);
	// hp.decrease_key(1, 6);
	// System.out.println(hp.extract_min());
	// System.out.println(hp.extract_min());
	// System.out.println(hp.extract_min());
	// }
}
