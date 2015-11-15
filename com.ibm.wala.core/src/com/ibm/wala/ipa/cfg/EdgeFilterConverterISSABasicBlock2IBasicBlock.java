/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.ipa.cfg;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.shrikeBT.IInstruction;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;

public class EdgeFilterConverterISSABasicBlock2IBasicBlock implements EdgeFilter<IBasicBlock<IInstruction>>{  
  private EdgeFilter<ISSABasicBlock> filter;
  final private ControlFlowGraph<SSAInstruction, ISSABasicBlock> original;

  public EdgeFilterConverterISSABasicBlock2IBasicBlock(EdgeFilter<ISSABasicBlock> filter,
      ControlFlowGraph<SSAInstruction, ISSABasicBlock> original) {
    super();
    this.filter = filter;
    this.original = original;
  }
  
  @Override
  public boolean hasNormalEdge(IBasicBlock<IInstruction> src, IBasicBlock<IInstruction> dst) {
     ISSABasicBlock nSrc = original.getNode(src.getNumber());
     ISSABasicBlock nDst = original.getNode(dst.getNumber());
     return filter.hasNormalEdge(nSrc, nDst);
  }

  @Override
  public boolean hasExceptionalEdge(IBasicBlock<IInstruction> src, IBasicBlock<IInstruction> dst) {
    ISSABasicBlock nSrc = original.getNode(src.getNumber());
    ISSABasicBlock nDst = original.getNode(dst.getNumber());
    return filter.hasExceptionalEdge(nSrc, nDst);  }

}
