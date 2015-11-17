/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.ssa;

import com.ibm.wala.analysis.arraybounds.ArrayOutOfBoundsAnalysis;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.classLoader.ShrikeIRFactory;
import com.ibm.wala.classLoader.SyntheticMethod;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.cfg.EdgeFilterConverterISSABasicBlock2IBasicBlock;
import com.ibm.wala.ipa.cfg.PrunedCFG;
import com.ibm.wala.ipa.cfg.exceptionpruning.ExceptionFilter2EdgeFilter;
import com.ibm.wala.ipa.cfg.exceptionpruning.filter.ArrayOutOfBoundFilter;
import com.ibm.wala.ipa.cfg.exceptionpruning.filter.CombinedExceptionFilter;
import com.ibm.wala.ipa.summaries.SyntheticIRFactory;
import com.ibm.wala.shrikeBT.IInstruction;
import com.ibm.wala.util.debug.Assertions;

/**
 * Default implementation of {@link IRFactory}.
 * 
 * This creates {@link IR} objects from Shrike methods, and directly from synthetic methods.
 */
public class DefaultIRFactory implements IRFactory<IMethod> {

  private final ShrikeIRFactory shrikeFactory = new ShrikeIRFactory();

  private final SyntheticIRFactory syntheticFactory = new SyntheticIRFactory();

  /*
   * @see com.ibm.wala.ssa.IRFactory#makeCFG(com.ibm.wala.classLoader.IMethod, com.ibm.wala.ipa.callgraph.Context,
   * com.ibm.wala.ipa.cha.IClassHierarchy, com.ibm.wala.util.warnings.WarningSet)
   */
  public ControlFlowGraph makeCFG(IMethod method, Context c) throws IllegalArgumentException {
    if (method == null) {
      throw new IllegalArgumentException("method cannot be null");
    }
    if (method.isSynthetic()) {
      return syntheticFactory.makeCFG((SyntheticMethod) method, c);
    } else if (method instanceof IBytecodeMethod) {
      return shrikeFactory.makeCFG((IBytecodeMethod) method, c);
    } else {
      Assertions.UNREACHABLE();
      return null;
    }
  }

  /*
   * @see com.ibm.wala.ssa.IRFactory#makeIR(com.ibm.wala.classLoader.IMethod, com.ibm.wala.ipa.callgraph.Context,
   * com.ibm.wala.ipa.cha.IClassHierarchy, com.ibm.wala.ssa.SSAOptions, com.ibm.wala.util.warnings.WarningSet)
   */
  @Override
  public IR makeIR(IMethod method, Context c, SSAOptions options) throws IllegalArgumentException {
    if (method == null) {
      throw new IllegalArgumentException("method cannot be null");
    }
    if (method.isSynthetic()) {
      return syntheticFactory.makeIR((SyntheticMethod) method, c, options);
    } else if (method instanceof IBytecodeMethod) {
      options.setPiNodePolicy(new AllDueToBranchePiPolicy());
      IR ir = shrikeFactory.makeIR((IBytecodeMethod) method, c, options);
      
      SSACFG cfg = ir.getControlFlowGraph();

      ArrayOutOfBoundsAnalysis arrayBoundsAnalysis = new ArrayOutOfBoundsAnalysis(ir);    

      CombinedExceptionFilter filter = new CombinedExceptionFilter();
      filter.add(new ArrayOutOfBoundFilter(arrayBoundsAnalysis));      

      ExceptionFilter2EdgeFilter<ISSABasicBlock> edgeFilter = new ExceptionFilter2EdgeFilter<ISSABasicBlock>(filter, cfg);
      EdgeFilterConverterISSABasicBlock2IBasicBlock convertedEdgeFilter = new EdgeFilterConverterISSABasicBlock2IBasicBlock(edgeFilter, cfg);       
      
      PrunedCFG<IInstruction, IBasicBlock<IInstruction>> prunedCfg = PrunedCFG.make(ir.getControlFlowGraph().delegate, convertedEdgeFilter);
      if (!prunedCfg.containsNode(prunedCfg.entry())){
        //make sure no optimisation will be performed and all things are actually executed.
        throw new RuntimeException("This exceptions should never make it to master branch.");
      }
      //ir.getControlFlowGraph().delegate = prunedCfg;
      
      return ir;
    } else {
      Assertions.UNREACHABLE();
      return null;
    }
  }

  /**
   * Is the {@link Context} irrelevant as to structure of the {@link IR} for a particular {@link IMethod}? 
   */
  @Override
  public boolean contextIsIrrelevant(IMethod method) {
    if (method == null) {
      throw new IllegalArgumentException("null method");
    }
    if (method.isSynthetic()) {
      return syntheticFactory.contextIsIrrelevant((SyntheticMethod) method);
    } else if (method instanceof ShrikeCTMethod) {
      // we know ShrikeFactory contextIsIrrelevant
      return true;
    } else {
      // be conservative .. don't know what's going on.
      return false;
    }
  }

}
