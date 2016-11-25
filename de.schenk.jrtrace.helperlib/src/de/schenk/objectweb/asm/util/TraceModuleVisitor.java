/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.schenk.objectweb.asm.util;

import de.schenk.objectweb.asm.ModuleVisitor;
import de.schenk.objectweb.asm.Opcodes;

/**
 * A {@link ModuleVisitor} that prints the fields it visits with a
 * {@link Printer}.
 * 
 * @author Remi Forax
 */
public final class TraceModuleVisitor extends ModuleVisitor {
    
    public final Printer p;

    public TraceModuleVisitor(final Printer p) {
        this(null, p);
    }

    public TraceModuleVisitor(final ModuleVisitor mv, final Printer p) {
        super(Opcodes.ASM6, mv);
        this.p = p;
    }

    @Override
    public void visitVersion(String version) {
        p.visitVersion(version);
        super.visitVersion(version);
    }
    @Override
    public void visitMainClass(String mainClass) {
        p.visitMainClass(mainClass);
        super.visitMainClass(mainClass);
    }
    @Override
    public void visitTargetPlatform(String osName, String osArch,
            String osVersion) {
        p.visitTargetPlatform(osName, osArch, osVersion);
        super.visitTargetPlatform(osName, osArch, osVersion);
    }
    @Override
    public void visitConcealedPackage(String packaze) {
        p.visitConcealedPackage(packaze);
        super.visitConcealedPackage(packaze);
    }
    
    @Override
    public void visitRequire(String module, int access) {
        p.visitRequire(module, access);
        super.visitRequire(module, access);
    }
    
    @Override
    public void visitExport(String packaze, int access, String... modules) {
        p.visitExport(packaze, access, modules);
        super.visitExport(packaze, access, modules);
    }
    
    @Override
    public void visitUse(String use) {
        p.visitUse(use);
        super.visitUse(use);
    }
    
    @Override
    public void visitProvide(String service, String impl) {
        p.visitProvide(service, impl);
        super.visitProvide(service, impl);
    }

    @Override
    public void visitEnd() {
        p.visitModuleEnd();
        super.visitEnd();
    }
}