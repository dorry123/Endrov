/**
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.biceps;

import org.apache.bcel.generic.*;
import org.apache.bcel.*;

public class ReturnBuilder
	{
  public static void main(String[] argv) {
    ClassGen cg = new ClassGen("BicepsReturn", "java.lang.Object", //extend
				      "<generated>", Constants.ACC_PUBLIC |
				      Constants.ACC_SUPER,
				      null);
    ConstantPoolGen cp = cg.getConstantPool(); 
    InstructionList il = new InstructionList();
    Type rtype=new ObjectType("foo");
    MethodGen mg = new MethodGen(/*Constants.ACC_STATIC |*/
				       Constants.ACC_PUBLIC,
				       Type.VOID,              // return type
				       new Type[] {rtype},            // argument types
				       new String[] { "inp" }, // arg names
				       "run", "BicepsReturn",    // method, class
				       il, cp);
    
    InstructionFactory factory = new InstructionFactory(cg);

    ObjectType i_stream = new ObjectType("java.io.InputStream");
    ObjectType p_stream = new ObjectType("java.io.PrintStream");

    /* Create BufferedReader object and store it in local variable `in'.
     */
    il.append(factory.createNew("java.io.BufferedReader"));
    il.append(InstructionConstants.DUP); // Use predefined constant, i.e. flyweight
    il.append(factory.createNew("java.io.InputStreamReader"));
    il.append(InstructionConstants.DUP);
    il.append(factory.createFieldAccess("java.lang.System", "in", i_stream,
					Constants.GETSTATIC));

    /* Call constructors, i.e. BufferedReader(InputStreamReader())
     */
    il.append(factory.createInvoke("java.io.InputStreamReader", "<init>",
				   Type.VOID, new Type[] { i_stream },
				   Constants.INVOKESPECIAL));
    il.append(factory.createInvoke("java.io.BufferedReader", "<init>", Type.VOID,
				   new Type[] { new ObjectType("java.io.Reader") },
				   Constants.INVOKESPECIAL));

    /* Create local variable `in'
     */
    LocalVariableGen lg = mg.addLocalVariable("in",
					      new ObjectType("java.io.BufferedReader"),
					      null, null);
    int in = lg.getIndex();
    lg.setStart(il.append(new ASTORE(in))); // `i' valid from here

    /* Create local variable `name'
     */
    lg = mg.addLocalVariable("name", Type.STRING, null, null);
    int name = lg.getIndex();
    il.append(InstructionConstants.ACONST_NULL);
    lg.setStart(il.append(new ASTORE(name))); // `name' valid from here

    /* try { ...
     */
    InstructionHandle try_start =
      il.append(factory.createFieldAccess("java.lang.System", "out", p_stream,
					  Constants.GETSTATIC));

    il.append(new PUSH(cp, "Please enter your name> "));
    il.append(factory.createInvoke("java.io.PrintStream", "print", Type.VOID, 
				   new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(new ALOAD(in));
    il.append(factory.createInvoke("java.io.BufferedReader", "readLine",
				   Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(new ASTORE(name));

    /* Upon normal execution we jump behind exception handler, 
     * the target address is not known yet.
     */
    GOTO g = new GOTO(null);
    InstructionHandle try_end = il.append(g);

    /* } catch() { ... }
     * Add exception handler: simply return from method
     */
    InstructionHandle handler = il.append(InstructionConstants.RETURN);
    mg.addExceptionHandler(try_start, try_end, handler,
			   new ObjectType("java.io.IOException"));

    /* Normal code continues, now we can set the branch target of the GOTO
     * that jumps over the handler code.
     */
    InstructionHandle ih =
      il.append(factory.createFieldAccess("java.lang.System", "out", p_stream,
					  Constants.GETSTATIC));
    g.setTarget(ih);

    /* String concatenation compiles to StringBuffer operations.
     */
    il.append(factory.createNew(Type.STRINGBUFFER));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(cp, "Hello, "));
    il.append(factory.createInvoke("java.lang.StringBuffer", "<init>",
				   Type.VOID, new Type[] { Type.STRING },
				   Constants.INVOKESPECIAL));
    il.append(new ALOAD(name));
    
    /* Concatenate strings using a StringBuffer and print them.
     */
    il.append(factory.createInvoke("java.lang.StringBuffer", "append",
				   Type.STRINGBUFFER, new Type[] { Type.STRING },
				   Constants.INVOKEVIRTUAL));
    il.append(factory.createInvoke("java.lang.StringBuffer", "toString",
				   Type.STRING, Type.NO_ARGS,
				   Constants.INVOKEVIRTUAL));
    
    il.append(factory.createInvoke("java.io.PrintStream", "println",
				   Type.VOID, new Type[] { Type.STRING },
				   Constants.INVOKEVIRTUAL));

    il.append(InstructionConstants.RETURN);

    mg.setMaxStack(5); // Needed stack size
    cg.addMethod(mg.getMethod());

    il.dispose(); // Reuse instruction handles

    /* Add public <init> method, i.e. empty constructor
     */
    cg.addEmptyConstructor(Constants.ACC_PUBLIC);

    /* Get JavaClass object and dump it to file.
     */
    try {
      cg.getJavaClass().dump("HelloWorld.class");
    } catch(java.io.IOException e) { System.err.println(e); }
  }
}
