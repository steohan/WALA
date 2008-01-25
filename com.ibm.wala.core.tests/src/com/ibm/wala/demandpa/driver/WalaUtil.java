/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * This file is a derivative of code released by the University of
 * California under the terms listed below.  
 *
 * Refinement Analysis Tools is Copyright �2007 The Regents of the
 * University of California (Regents). Provided that this notice and
 * the following two paragraphs are included in any distribution of
 * Refinement Analysis Tools or its derivative work, Regents agrees
 * not to assert any of Regents' copyright rights in Refinement
 * Analysis Tools against recipient for recipient�s reproduction,
 * preparation of derivative works, public display, public
 * performance, distribution or sublicensing of Refinement Analysis
 * Tools and derivative works, in source code and object code form.
 * This agreement not to assert does not confer, by implication,
 * estoppel, or otherwise any license or rights in any intellectual
 * property of Regents, including, but not limited to, any patents
 * of Regents or Regents� employees.
 * 
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT,
 * INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES,
 * INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE
 * AND ITS DOCUMENTATION, EVEN IF REGENTS HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *   
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE AND FURTHER DISCLAIMS ANY STATUTORY
 * WARRANTY OF NON-INFRINGEMENT. THE SOFTWARE AND ACCOMPANYING
 * DOCUMENTATION, IF ANY, PROVIDED HEREUNDER IS PROVIDED "AS
 * IS". REGENTS HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package com.ibm.wala.demandpa.driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.debug.Trace;
import com.ibm.wala.util.warnings.WalaException;

/**
 * Various utility methods for working with WALA.
 * @author Manu Sridharan
 *
 */
public class WalaUtil {

  public static void initializeTraceFile() {
    Properties prop = null;
    ;
    try {
      prop = WalaProperties.loadProperties();
    } catch (WalaException e) {
      e.printStackTrace();
      Assertions.UNREACHABLE();
    }
  
    String outputDir = prop.getProperty(WalaProperties.OUTPUT_DIR);
    String fileName = outputDir + File.separator + "trace.txt";
    // String fileName = outputDir + File.separator + "perf.txt";
  
    Trace.setTraceFile(fileName);
  }

  public static void dumpAllIR(CallGraph cg, String benchName, Properties p) throws IllegalArgumentException, IllegalArgumentException {
    if (cg == null) {
      throw new IllegalArgumentException("cg == null");
    }
    if (p == null) {
      throw new IllegalArgumentException("p == null");
    }
    System.err.print("dumping ir...");
    String irFile = p.getProperty(WalaProperties.OUTPUT_DIR) + File.separatorChar + benchName + "-ir.txt";
    try {
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(irFile)));
      for (Iterator<? extends CGNode> iter = cg.iterator(); iter.hasNext();) {
        CGNode node = iter.next();
        IR ir = node.getIR();
        if (ir == null)
          continue;
        writer.println(node);
        writer.println("+++++++++++++++++++++++++++++++++");
        writer.println(ir);
        writer.println("");
      }
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  
    System.err.println("done");
  }

}