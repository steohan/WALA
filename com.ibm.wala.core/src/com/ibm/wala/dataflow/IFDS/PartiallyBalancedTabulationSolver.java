/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.dataflow.IFDS;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;

/**
 * Utilities for dealing with tabulation with partially balanced parentheses.
 * 
 * @author sjfink
 * 
 */
public class PartiallyBalancedTabulationSolver<T, P> extends TabulationSolver<T, P> {

  public static <T, P> PartiallyBalancedTabulationSolver<T, P> createPartiallyBalancedTabulationSolver(
      PartiallyBalancedTabulationProblem<T, P> p, IProgressMonitor monitor) {
    return new PartiallyBalancedTabulationSolver<T, P>(p, monitor);
  }

  private final Collection<P> seedProcedures = HashSetFactory.make();

  protected PartiallyBalancedTabulationSolver(PartiallyBalancedTabulationProblem<T, P> p, IProgressMonitor monitor) {
    super(p, monitor);
  }

  @Override
  protected void propagate(T s_p, int i, T n, int j) {
    super.propagate(s_p, i, n, j);
    if (isExitFromSeedMethod(n)) {
      // j was reached from an entry seed. if there are any facts which are reachable from j, even without
      // balanced parentheses, we can use these as new seeds.
      for (Iterator<? extends T> it2 = supergraph.getSuccNodes(n); it2.hasNext();) {
        T retSite = it2.next();
        PartiallyBalancedTabulationProblem<T, P> problem = (PartiallyBalancedTabulationProblem<T, P>) getProblem();
        IFlowFunction f = problem.getFunctionMap().getUnbalancedReturnFlowFunction(n, retSite);
        // for each fact that can be reached by the return flow ...
        if (f instanceof IUnaryFlowFunction) {
          IUnaryFlowFunction uf = (IUnaryFlowFunction) f;
          IntSet facts = uf.getTargets(j);
          if (facts != null) {
            for (IntIterator it4 = facts.intIterator(); it4.hasNext();) {
              int d3 = it4.next();
              // d3 would be reached if we ignored parentheses. use it as a new seed.
              T fakeEntry = problem.getFakeEntry(retSite);
              PathEdge<T> seed = PathEdge.createPathEdge(fakeEntry, d3, retSite, d3);
              addSeed(seed);
            }
          }
        } else {
          Assertions.UNREACHABLE("Partially balanced logic not supported for binary return flow functions");
        }
      }
    }
  }

  @Override
  public void addSeed(PathEdge<T> seed) {
    seedProcedures.add(getSupergraph().getProcOf(seed.entry));
    super.addSeed(seed);

  }

  /**
   * Is n an exit from a procedure from which a seed originated
   */
  private boolean isExitFromSeedMethod(T n) {
    if (getSupergraph().isExit(n) && seedProcedures.contains(getSupergraph().getProcOf(n))) {
      return true;
    }
    return false;
  }
}