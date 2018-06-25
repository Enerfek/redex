/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.redex.test.instr;

import static org.fest.assertions.api.Assertions.*;

import org.junit.Test;

/**
The Alpha class has a private field which is accessed from
an inner class Beta which causes Java to induce a synthetic
getter. Redex removes this getter and in-lines the access
to the field. The test below ensures this optimization does
not break the generated bytecode.
*/

class Alpha {

  public Alpha(int v) {
    x = v;
  }

  private static int x;

  public class Beta {
    public int doublex() {
      return 2 * x; // Access to private field in outer class.
    }
  }
}

/**
This example below is designed to make sure we do not remove synthetic
methods that refer to non-concrete fields. The invoke-static that
arises from the use of this.out creates a reference to a field that
is defined in a different compilation unit (java.io.FilterWriter):

0004fc: 5410 0200                              |0000: iget-object v0, v1, Lcom/facebook/redextest/Gamma$Delta;.this$0:Lcom/facebook/redextest/Gamma; // field@0002
000500: 7110 0b00 0000                         |0002: invoke-static {v0}, Lcom/facebook/redextest/Gamma;.access$000:(Lcom/facebook/redextest/Gamma;)Ljava/io/Writer; // method@000b
000506: 0c00                                   |0005: move-result-object v0
000508: 1100                                   |0006: return-object v0


We must not optimize this synthetic getter. If we do we get a bad access to
a protected field when getWriter is called:

invoke-virtual {v2}, Lcom/facebook/redextest/Gamma$Delta;.getWriter:()Ljava/io/Writer; // method@0008

*/

class Gamma extends java.io.FilterWriter {

  public Gamma(java.io.Writer writer) {
    super(writer);
  }

  class Delta {
    public java.io.Writer getWriter() {
      return out; // Access to non-concrete field.
    }
  }
}

public class SafeSynthGetterRemoval {

    /**
     * This test checks that synthetic getters removal
     * does not break the generated bytecode. The first
     * test involving the class Alpha checks a case when
     * a synthetic getter can be safely removed, and the second
     * case tests a case when a synthetic getter can not be removed
     * because it refers to a non-concrete field.
     */
    @Test
    public void test() {

      // First test that the Alpha and Alpha.Beta classes work.
      Alpha a = new Alpha(12);
      Alpha.Beta b = a.new Beta();
      assertThat(b.doublex()).isEqualTo(24);

      // Now test the Gamma and Gamma.Delta classes.
      String s = "";
      String expected = "hello";
      try {
      	java.io.Writer writer = new java.io.StringWriter();
      	writer.write(expected);
      	Gamma g = new Gamma(writer);
      	Gamma.Delta d = g.new Delta();
      	s = d.getWriter().toString();
      } catch (java.io.IOException e) {
      	s = e.toString();
      }
      assertThat(s).isEqualTo(expected);
    }
}
