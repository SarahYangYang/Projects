package 531_1;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Graph {

	int NodeNumber;
	int EdgeNumber;
	int[][] w;
	int[] pi;
	final int INF = Integer.MAX_VALUE;
	ArrayList<ArrayList<Integer>> tree;

	Graph() {
	}

	void InsertEdge(int u, int v, int weight) {
		w[u][v] = weight;
		w[v][u] = weight;
	}

	void ReadGraphFromFile(String inputFile) throws IOException {
		Path filePath = Paths.get(inputFile);
		Scanner scanner = new Scanner(filePath);
		NodeNumber = scanner.nextInt();
		EdgeNumber = scanner.nextInt();
		w = new int[NodeNumber + 1][NodeNumber + 1];
		for (int i = 1; i <= NodeNumber; i++) {
			for (int j = 1; j <= NodeNumber; j++) {
				w[i][j] = w[j][i] = INF;
			}
		}

		for (int i = 0; i < EdgeNumber; i++) {
			int u = scanner.nextInt();
			int v = scanner.nextInt();
			int weight = scanner.nextInt();
			w[u][v] = w[v][u] = weight;
		}
		scanner.close();
	}

	void MST_Prim() {
		boolean[] S = new boolean[NodeNumber + 1];
		int[] d = new int[NodeNumber + 1];
		pi = new int[NodeNumber + 1];
		Heap hp = new Heap(NodeNumber);
		d[1] = 0;
		S[1] = true;
		hp.insert(1, d[1]);
		for (int i = 2; i <= NodeNumber; i++) {
			S[i] = false;
			d[i] = INF;
			hp.insert(i, d[i]);
		}
		for (int i = 1; i <= NodeNumber; i++) {
			int u = hp.extract_min();
			S[u] = true;
			for (int v = 1; v <= NodeNumber; v++) {
				if (S[v] == false && w[u][v] != INF) {
					if (w[u][v] < d[v]) {
						d[v] = w[u][v];
						hp.decrease_key(v, d[v]);
						pi[v] = u;
					}
				}
			}
		}

		// build tree
		tree = new ArrayList<ArrayList<Integer>>();
		tree.add(null);
		for (int i = 1; i <= NodeNumber; i++) {
			tree.add(new ArrayList<Integer>());
		}
		for (int i = 2; i <= NodeNumber; i++) {
			(tree.get(i)).add(pi[i]);
			(tree.get(pi[i])).add(i);
		}
	}

	void OutputToFile(String ouputFile) throws IOException {
		FileWriter fw = new FileWriter(ouputFile);
		int totalWeight = 0;

		for (int i = 1; i <= NodeNumber; i++) {
			totalWeight += w[i][pi[i]];
		}
		fw.write(String.valueOf(totalWeight) + "\n");
		for (int i = 1; i <= NodeNumber; i++) {
			ArrayList<Integer> neighbors = tree.get(i);
			Iterator<Integer> it = neighbors.iterator();
			while (it.hasNext()) {
				int n = it.next();
				if (n > i) {
					fw.write(String.valueOf(i) + " " + String.valueOf(n) + " " + String.valueOf(w[i][n]) + "\n");
				}
			}
		}
		fw.close();
	}

	public static void main(String args[]) throws IOException {
		Graph g = new Graph();
		g.ReadGraphFromFile("./P1_Public_Cases/est5");
		g.MST_Prim();
		g.OutputToFile("./P1_Public_Cases/output5");
	}
}
