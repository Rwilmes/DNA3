package dna.metrics.clusterCoefficient;

import dna.graph.IElement;
import dna.graph.edges.DirectedEdge;
import dna.graph.nodes.DirectedNode;
import dna.graph.nodes.Node;
import dna.series.data.NodeValueList;
import dna.updates.batch.Batch;
import dna.updates.update.EdgeAddition;
import dna.updates.update.EdgeRemoval;
import dna.updates.update.NodeAddition;
import dna.updates.update.NodeRemoval;
import dna.updates.update.Update;
import dna.util.ArrayUtils;

public class DirectedClusteringCoefficientU extends
		DirectedClusteringCoefficient {

	public DirectedClusteringCoefficientU() {
		super("DirectedClusteringCoefficientU",
				ApplicationType.BeforeAndAfterUpdate, MetricType.exact);
	}

	@Override
	public boolean applyBeforeBatch(Batch b) {
		return false;
	}

	@Override
	public boolean applyAfterBatch(Batch b) {
		return false;
	}

	@Override
	public boolean applyBeforeUpdate(Update u) {
		if (u instanceof EdgeRemoval) {
			DirectedEdge e = (DirectedEdge) ((EdgeRemoval) u).getEdge();
			DirectedNode a = e.getSrc();
			DirectedNode b = e.getDst();

			// t1
			for (IElement cUncasted : a.getNeighbors()) {
				DirectedNode c = (DirectedNode) cUncasted;
				if (b.hasNeighbor(c)) {
					this.removeTriangle(c);
				}
			}

			// t2 / t3
			if (a.hasNeighbor(b)) {
				// t2
				for (IElement cUncasted : a.getNeighbors()) {
					DirectedNode c = (DirectedNode) cUncasted;
					if (!a.hasNeighbor(b)) {
						continue;
					}
					if (c.hasEdge(new DirectedEdge(c, b))) {
						this.removeTriangle(a);
					}
					if (c.hasEdge(new DirectedEdge(b, c))) {
						this.removeTriangle(a);
					}
				}

				// t3
				for (IElement cUncasted : b.getNeighbors()) {
					DirectedNode c = (DirectedNode) cUncasted;
					if (c.hasEdge(new DirectedEdge(c, a))) {
						this.removeTriangle(b);
					}
					if (c.hasEdge(new DirectedEdge(a, c))) {
						this.removeTriangle(b);
					}
				}

			}

			// p
			if (a.hasNeighbor(b)) {
				this.removePotentials(a, 2 * (a.getNeighborCount() - 1));
				this.removePotentials(b, 2 * (b.getNeighborCount() - 1));
			}
		}
		return true;
	}

	@Override
	public boolean applyAfterUpdate(Update u) {
		if (u instanceof NodeAddition) {
			Node n = (Node) ((NodeAddition) u).getNode();
			this.localCC.setValue(n.getIndex(), 0);
			this.nodePotentialCount = ArrayUtils.set(this.nodePotentialCount,
					n.getIndex(), 0, Long.MIN_VALUE);
			this.nodeTriangleCount = ArrayUtils.set(this.nodeTriangleCount,
					n.getIndex(), 0, Long.MIN_VALUE);
			this.averageCC = ArrayUtils.avgIgnoreNaN(this.localCC.getValues());
		} else if (u instanceof NodeRemoval) {

			DirectedNode a = (DirectedNode) ((NodeRemoval) u).getNode();

			// t1
			for (IElement bUncasted : a.getNeighbors()) {
				DirectedNode b = (DirectedNode) bUncasted;
				for (IElement cUncasted : a.getNeighbors()) {
					DirectedNode c = (DirectedNode) cUncasted;
					if (b.equals(c)) {
						continue;
					}
					if (b.hasEdge(new DirectedEdge(b, c))) {
						this.removeTriangle(a);
					}
				}
			}

			// t2
			for (IElement bUncasted : a.getNeighbors()) {
				DirectedNode b = (DirectedNode) bUncasted;
				for (IElement cUncasted : b.getNeighbors()) {
					DirectedNode c = (DirectedNode) cUncasted;
					if (a.hasEdge(new DirectedEdge(a, c))) {
						this.removeTriangle(b);
					}
					if (a.hasEdge(new DirectedEdge(c, a))) {
						this.removeTriangle(b);
					}
				}
			}

			// p1
			this.removePotentials(a,
					a.getNeighborCount() * (a.getNeighborCount() - 1));

			// p2
			for (IElement bUncasted : a.getNeighbors()) {
				DirectedNode b = (DirectedNode) bUncasted;
				this.removePotentials(b, b.getNeighborCount() * 2);
			}

			this.localCC.setValue(a.getIndex(), NodeValueList.emptyValue);
			this.nodePotentialCount[a.getIndex()] = Long.MIN_VALUE;
			this.nodeTriangleCount[a.getIndex()] = Long.MIN_VALUE;
			this.localCC.truncate();
			this.nodePotentialCount = ArrayUtils.truncate(
					this.nodePotentialCount, Long.MIN_VALUE);
			this.nodeTriangleCount = ArrayUtils.truncate(
					this.nodeTriangleCount, Long.MIN_VALUE);

			this.averageCC = ArrayUtils.avgIgnoreNaN(this.localCC.getValues());

		} else if (u instanceof EdgeAddition) {
			DirectedEdge e = (DirectedEdge) ((EdgeAddition) u).getEdge();
			DirectedNode a = e.getSrc();
			DirectedNode b = e.getDst();

			// t1
			for (IElement cUncasted : a.getNeighbors()) {
				DirectedNode c = (DirectedNode) cUncasted;
				if (b.hasNeighbor(c)) {
					this.addTriangle(c);
				}
			}

			// t2 / t3
			if (a.hasNeighbor(b)) {

				// t2
				for (IElement cUncasted : a.getNeighbors()) {
					DirectedNode c = (DirectedNode) cUncasted;
					if (!a.hasNeighbor(b)) {
						continue;
					}
					if (c.hasEdge(new DirectedEdge(c, b))) {
						this.addTriangle(a);
					}
					if (c.hasEdge(new DirectedEdge(b, c))) {
						this.addTriangle(a);
					}
				}

				// t3
				for (IElement cUncasted : b.getNeighbors()) {
					DirectedNode c = (DirectedNode) cUncasted;
					if (c.hasEdge(new DirectedEdge(c, a))) {
						this.addTriangle(b);
					}
					if (c.hasEdge(new DirectedEdge(a, c))) {
						this.addTriangle(b);
					}
				}

			}

			// p
			if (a.hasNeighbor(b)) {
				this.addPotentials(a, 2 * (a.getNeighborCount() - 1));
				this.addPotentials(b, 2 * (b.getNeighborCount() - 1));
			}

		}
		return true;
	}

	@Override
	public boolean compute() {
		this.triangleCount = 0;
		this.potentialCount = 0;
		for (IElement nUncasted : g.getNodes()) {
			DirectedNode n = (DirectedNode) nUncasted;
			this.nodeTriangleCount[n.getIndex()] = 0;
			this.nodePotentialCount[n.getIndex()] = 0;
			for (IElement uUncasted : n.getNeighbors()) {
				DirectedNode u = (DirectedNode) uUncasted;
				for (IElement vUncasted : n.getNeighbors()) {
					DirectedNode v = (DirectedNode) vUncasted;
					if (u.equals(v)) {
						continue;
					}
					this.nodePotentialCount[n.getIndex()]++;
					if (u.hasEdge(new DirectedEdge(u, v))) {
						this.nodeTriangleCount[n.getIndex()]++;
					}
				}
			}
			this.triangleCount += this.nodeTriangleCount[n.getIndex()];
			this.potentialCount += this.nodePotentialCount[n.getIndex()];
			if (this.nodePotentialCount[n.getIndex()] == 0) {
				this.localCC.setValue(n.getIndex(), 0);
			} else {
				this.localCC
						.setValue(
								n.getIndex(),
								(double) this.nodeTriangleCount[n.getIndex()]
										/ (double) this.nodePotentialCount[n
												.getIndex()]);
			}
		}

		if (this.potentialCount == 0) {
			this.globalCC = 0;
		} else {
			this.globalCC = (double) this.triangleCount
					/ (double) this.potentialCount;
		}
		this.averageCC = ArrayUtils.avg(this.localCC.getValues());

		return true;
	}

}
