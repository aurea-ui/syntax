/*
Syntax is distributed under the Revised, or 3-clause BSD license
===============================================================================
Copyright (c) 1985, 2012, 2016, Jaime Garza
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the copyright holder nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
===============================================================================
*/
package me.jaimegarza.syntax.model.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * A node for a dfa
 * @author jgarza
 *
 */
public class NfaNode extends Node {

  private static int sequence = 0;

  /**
   * Default constructor
   * @param graph the NFA to which this node belongs to
   */
  public NfaNode(Nfa graph) {
    super(graph, sequence++);
  }
  
  /**
   * Compute the &epsilon;-closure of the node by traversing all &epsilon; transitions
   * @param closure a set where the &epsilon;-closures are placed.
   */
  protected void eclosure(Set<NfaNode> closure) {
    closure.add(this);
    for (Transition t: transitions) {
      if (t.isEpsilon() && !closure.contains(t.getTo())) {
        ((NfaNode) t.getTo()).eclosure(closure);
      }
    }
  }
  
  @Override
  public Set<NfaNode> eclosure() {
    Set<NfaNode> closure = new HashSet<NfaNode>();
    eclosure(closure);
    return closure;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    
    try {
      NfaNode n = (NfaNode) o;
      return n.id == id;
    } catch (ClassCastException unused) {
      return false;
    } catch (NullPointerException unused) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Needed for test cases
   */
  public static void reset() {
    sequence = 0;
  }
}
