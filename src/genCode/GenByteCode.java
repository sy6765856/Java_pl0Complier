package genCode;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import genCode.attribute_struct.*;
import genCode.constant_struct.*;
import genCode.fields_struct.*;
import genCode.method_struct.*;
import genCode.store.VarStack;

public class GenByteCode {

	private short[] magic;
	private byte[] minor_version;
	private byte[] major_version;
	private short constant_count;
	private ArrayList<Cp_info> constant_pool;
	private byte[] access_flags;
	private short thisClass;
	private short superClass;
	private short interface_count;
	private short fields_count;
	private ArrayList<Fields_info> fields_pool;
	private short methods_count;
	private ArrayList<Method_info> methods_pool;
	private short attributes_count;
	private ArrayList<Attribute_info> attribute_pool;
	private FileOutputStream out;
	private short index_Constant;
	private HashMap<String, Short> constantLineNO;
	private HashMap<String, Code> codeMap; // funcation , code
	private HashMap<String, VarStack> varStackStructMap; // function VarStack
	private ArrayList<Integer> gotoStack;
	private ArrayList<Integer> quitLoopStack;

	public GenByteCode()
    {
		magic = new short[] { 0xca, 0xfe, 0xba, 0xbe };// 0xCAFEBABE
		minor_version = new byte[] { 0x00, 0x00 };
		major_version = new byte[] { 0x00, 0x32 };
		access_flags = new byte[] { 0x0, 0x21 };
		constant_count = 1;
		interface_count = 0; // no interface

		gotoStack = new ArrayList<Integer>();
		quitLoopStack = new ArrayList<Integer>();
		codeMap = new HashMap<String, Code>();
		varStackStructMap = new HashMap<String, VarStack>();
		constant_pool = new ArrayList<Cp_info>();
		fields_pool = new ArrayList<Fields_info>();
		methods_pool = new ArrayList<Method_info>();
		attribute_pool = new ArrayList<Attribute_info>();
		constantLineNO = new HashMap<String, Short>();
	}

	public HashMap<String, Short> getConstantLineNO()
    {
		return constantLineNO;
	}

	public void setConstantLineNO(HashMap<String, Short> constantLineNO)
    {
		this.constantLineNO = constantLineNO;
	}

	public void genClassFile(String fileName)
    {
		try
        {
			out = new FileOutputStream(fileName + ".class");
			this.outHeader();// magic and version
			this.outConstant();
			this.outAccessFlag();
			this.outThisClass();
			this.outSuperClass();
			this.outInterface_count();
			this.outFields();
			this.outMethods();
			this.outAttribute();
			out.close();
			// System.out.println("\n\nRuning java <classfile> ... \nDecopile Class File <javap -c classfile> ...");
			// Process p = Runtime.getRuntime().exec("javap -c " + fileName);
			// InputStream is = p.getInputStream();
			// InputStreamReader isr = new InputStreamReader(is);
			// BufferedReader br = new BufferedReader(isr);
			// String line = br.readLine();
			// while ((line = br.readLine()) != null) {
			// 	System.out.println(line);
			// }
			// isr.close();
			// br.close();
			// is.close();

			// System.out.println("Execute ClassFile <java classfile> ... \nResult:");
			// p = Runtime.getRuntime().exec("java " + fileName);
			// is = p.getInputStream();
			// isr = new InputStreamReader(is);
			// br = new BufferedReader(isr);
			// while ((line = br.readLine()) != null) {
			// 	System.out.println(line);
			// }
			// isr.close();
			// br.close();
			// is.close();
		} catch (Exception e) {e.printStackTrace();}
	}

	public HashMap<String, VarStack> getStackMap()
    {
		return this.varStackStructMap;
	}

	public void addCp_info(Cp_info cp)
    {
		this.constant_count++;
		this.constant_pool.add(cp);
	}

	public void addFields_info(Fields_info fields)
    {
		this.fields_count++;
		this.fields_pool.add(fields);
	}

	public void addMethod_info(Method_info method)
    {
		this.methods_count++;
		this.methods_pool.add(method);
	}

	public void addAttribute_info(Attribute_info attribute)
    {
		this.attributes_count++;
		this.attribute_pool.add(attribute);
	}

	private void outHeader() throws Exception
    {
		for (short i : magic){out.write(i);}
		out.write(minor_version);
		out.write(major_version);
	}

	private void outConstant() throws Exception
    {
		this.outConstant_Count();
		this.outConstant_pool();
	}

	private void outFields() throws Exception
    {
		this.outFields_count();
		this.outFields_pool();
	}

	private void outMethods() throws Exception
    {
		this.outMethods_count();
		this.outMethods_pool();
	}

	private void outAttribute() throws Exception
    {
		this.outAttributes_count();
		this.outAttribute_pool();
	}

	private void outConstant_Count() throws Exception
    {
		outShort(constant_count);
	}

	private void outAccessFlag() throws Exception
    {
		out.write(this.access_flags);
	}

	private void outThisClass() throws Exception
    {
		outShort(thisClass);
	}

	private void outSuperClass() throws Exception
    {
		outShort(superClass);
	}

	private void outInterface_count() throws Exception
    {
		outShort(interface_count);
	}

	private void outFields_count() throws Exception
    {
		outShort(fields_count);
	}

	private void outMethods_count() throws Exception
    {
		outShort(methods_count);
	}

	private void outAttributes_count() throws Exception
    {
		outShort(attributes_count);
	}

	private void outShort(short sh) throws Exception
    {
		byte[] b1 = new byte[2];
		for (int i = 0; i < 2; i++)
        {
			b1[1 - i] = (byte) ((sh >> i * 8) & 0xff);
		}
		out.write(b1);
	}

	private void outConstant_pool() throws Exception
    {
		for (Cp_info cp : this.constant_pool)
        {
			cp.outCp_infoByte(out);
		}
	}

	private void outFields_pool() throws Exception
    {
		for (int i = 0; i < fields_count; i++)
        {
			fields_pool.get(i).outMethodBytes(out);
		}
	}

	private void outMethods_pool() throws Exception
    {
		for (Method_info m : this.methods_pool)
        {
			m.outMethodBytes(out);
		}
	}

	private void outAttribute_pool() throws Exception
    {
		for (Attribute_info a : attribute_pool)
        {
			a.outAttributeByte(out);
		}
	}

	public byte[] int2bytes(int i)
    {
		byte[] b = new byte[4];
		for (int j = 0; j < 4; j++)
        {
			b[3 - j] = (byte) ((i >> j * 8) & 0xff);
		}
		return b;
	}

	public byte[] short2bytes(short sh)
    {
		byte[] b = new byte[2];
		for (int i = 0; i < 2; i++)
        {
			b[1 - i] = (byte) ((sh >> i * 8) & 0xff);
		}
		return b;
	}

	public void genSkeleton(String classname)
    {
		// construct function <init> 1: 0a 00 02 00 03
		Cp_info cp = new CONSTANT_Methodref_info();
		((CONSTANT_Methodref_info) cp).setClass_index((short) 0x03);
		((CONSTANT_Methodref_info) cp).setName_and_type_index((short) 0x04);
		index_Constant++;
		this.addCp_info(cp);

		// 2: 07 00 point to this class
		cp = new CONSTANT_Class_info();
		((CONSTANT_Class_info) cp).setName_index((short) 0xd);
		index_Constant++;
		this.constantLineNO.put("this", this.index_Constant);
		this.addCp_info(cp);

		// 3: 07 00 point to superclass
		cp = new CONSTANT_Class_info();
		((CONSTANT_Class_info) cp).setName_index((short) 0xe);
		index_Constant++;
		this.constantLineNO.put("super", this.index_Constant);
		this.addCp_info(cp);

		// CONSTANT_NameAndType_info of <init> 4:0c 00 05 00 06
		cp = new CONSTANT_NameAndType_info();
		((CONSTANT_NameAndType_info) cp).setName_index((short) 0x05);
		((CONSTANT_NameAndType_info) cp).setDescriptor_index((short) 0x06);
		this.addCp_info(cp);
		index_Constant++;

		// <init> 5: 01 00 06 ...<init>
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("<init>").getBytes());
		this.addCp_info(cp);
		index_Constant++;
		this.constantLineNO.put("init", this.index_Constant);

		// ()V 6: 01 00 03 ... ()V
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("()V").getBytes());
		this.addCp_info(cp);
		index_Constant++;
		this.constantLineNO.put("()V", this.index_Constant);

		// Code 7: 01 00 04 43 6f 64 65
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("Code").getBytes());
		this.addCp_info(cp);
		index_Constant++;
		this.constantLineNO.put("Code", this.index_Constant);

		// LineNumberTable 8:01 00 0f ....
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("LineNumberTable")
				.getBytes());
		this.addCp_info(cp);
		index_Constant++;
		this.constantLineNO.put("LineNumberTable", this.index_Constant);

		// init function
		Method_info init = new Method_info();
		this.varStackStructMap.put("init", new VarStack());
		init.setAccess_flags((short) 0x01);// public
		init.setName_index(this.constantLineNO.get("init"));
		init.setDescriptor_index(this.constantLineNO.get("()V"));
		Code code = new Code();
		code.setAttribute_name_index(this.constantLineNO.get("Code"));
		code.setMax_stack((short) 0x01);
		code.setMax_locals((short) 0x01);
		code.addCodeByte(new int[] { 0x2a, 0xb7, 0x00, 0x01, 0xb1 });
		init.addAttributes(code);
		this.addMethod_info(init);

		// main function 9:01 00 04 .. main
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("main").getBytes());
		this.addCp_info(cp);
		index_Constant++;
		this.constantLineNO.put("main", index_Constant);
		// main args 10:01 00 16 ...
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("([Ljava/lang/String;)V")
				.getBytes());
		this.addCp_info(cp);
		index_Constant++;
		this.constantLineNO.put("main_args", index_Constant);

		Method_info main = new Method_info();
		VarStack vs = new VarStack();
		vs.addCurrenPc();
		this.varStackStructMap.put("main", vs);
		main.setAccess_flags((short) 0x09);
		main.setName_index(this.constantLineNO.get("main"));
		main.setDescriptor_index(this.constantLineNO.get("main_args"));
		code = new Code();
		code.setAttribute_name_index(this.constantLineNO.get("Code"));
		code.setMax_stack((short) 0xff);
		code.setMax_locals((short) 0x1);
		// code.addCodeByte(new int[] { 0xb1 });
		codeMap.put("main", code);
		main.addAttributes(code);
		this.addMethod_info(main);

		// SourceFile 11:01 00 0a 53 6f 75 72 63 65 46 69 6c 65
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("SourceFile").getBytes());
		this.addCp_info(cp);
		this.index_Constant++;
		this.constantLineNO.put("SourceFile", index_Constant);

		// 12 : xxx.java
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte((classname + ".java").getBytes());
		this.addCp_info(cp);
		this.index_Constant++;
		this.constantLineNO.put(classname + ".java", index_Constant);

		SourceFile sf = new SourceFile();
		sf.setAttribute_name_index(this.constantLineNO.get("SourceFile"));
		sf.setSourcefile_index(this.constantLineNO.get(classname + ".java"));
		this.addAttribute_info(sf);

		// thisclass 13:this class
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(classname.getBytes());
		this.addCp_info(cp);
		this.index_Constant++;
		this.thisClass = this.constantLineNO.get("this");

		// add super class 14:01 00 10 java/lang/Object
		cp = new CONSTANT_Utf8_info();
		((CONSTANT_Utf8_info) cp).addByte(new String("java/lang/Object").getBytes());
		this.addCp_info(cp);
		index_Constant++;
		this.superClass = this.constantLineNO.get("super");
	}

	public void genConstFloat(String constname, float value) {
		Cp_info cp = new CONSTANT_Float_info();
		int f = Float.floatToRawIntBits(value);
		((CONSTANT_Float_info) cp).setBytes(int2bytes(f));
		this.index_Constant++;
		this.constantLineNO.put(constname, index_Constant);
		this.addCp_info(cp);
	}

	public int genFloat(float value)
    {
		Cp_info cp = new CONSTANT_Float_info();
		int f = Float.floatToRawIntBits(value);
		((CONSTANT_Float_info) cp).setBytes(int2bytes(f));
		this.index_Constant++;
		this.addCp_info(cp);
		return this.index_Constant;
	}

	public void genConstInt(String constname, int value)
    {
		Cp_info cp = new CONSTANT_Integer();
		((CONSTANT_Integer) cp).setBytes(value);
		this.index_Constant++;
		this.constantLineNO.put(constname, this.index_Constant);
		this.addCp_info(cp);
	}

	public int genInt(int value)
    {
		Cp_info cp = new CONSTANT_Integer();
		((CONSTANT_Integer) cp).setBytes(value);
		this.index_Constant++;
		this.addCp_info(cp);
		return this.index_Constant;
	}

	public void genFun(String funcName, String args, short args_num)
    {
		// constant
		// System.out.println(args);
		Cp_info cp = new CONSTANT_Methodref_info();
		this.index_Constant++;
		this.addCp_info(cp);
		((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
				.get("this"));
		((CONSTANT_Methodref_info) cp)
				.setName_and_type_index((short) (this.index_Constant + 1));
		Cp_info cpNameAndType = new CONSTANT_NameAndType_info();
		this.index_Constant++;
		this.addCp_info(cpNameAndType);
		((CONSTANT_NameAndType_info) cpNameAndType)
				.setName_index((short) (this.index_Constant + 1));
		cp = new CONSTANT_Utf8_info();
		this.index_Constant++;
		this.constantLineNO.put(funcName, this.index_Constant);
		this.addCp_info(cp);
		((CONSTANT_Utf8_info) cp).addByte(funcName.getBytes());
		if (this.constantLineNO.get(args) == null) {
			((CONSTANT_NameAndType_info) cpNameAndType)
					.setDescriptor_index((short) (this.index_Constant + 1));
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			this.constantLineNO.put(args, this.index_Constant);
			this.addCp_info(cp);
			((CONSTANT_Utf8_info) cp).addByte(args.getBytes());
		} else {
			((CONSTANT_NameAndType_info) cpNameAndType)
					.setDescriptor_index(this.constantLineNO.get(args));
		}

		//method
		Method_info method = new Method_info();
		method.setAccess_flags((short) 0x09);
		method.setName_index(this.constantLineNO.get(funcName));
		method.setDescriptor_index(this.constantLineNO.get(args));
		Code code = new Code();
		code.setAttribute_name_index(this.constantLineNO.get("Code"));
		code.setMax_stack((short) 0xff);
		code.setMax_locals(args_num);
		this.codeMap.put(funcName, code);
		method.addAttributes(code);
		this.addMethod_info(method);
	}

	public void genAssignValue(String funcname, String varname,ArrayList<Integer> insts, String vartype)
    {
		VarStack vs;
		int pc;
		Code code = this.codeMap.get(funcname);
		vs = this.varStackStructMap.get(funcname);
		if (vs.getVar_indexMap().get(varname) == null)
        {
			vs.getVar_indexMap().put(varname, vs.getCurrentPc());
			pc = vs.getCurrentPc();
			vs.addCurrenPc();
			code.addMaxLocals();
		}
        else pc = vs.getVar_indexMap().get(varname);
		if (vartype.equals("integer"))
        {
			// istore var
			insts.add(0x36);
		}
        else if (vartype.equals("real"))insts.add(0x38);
		else if (vartype.equals("char"))insts.add(0x36);
		else if (vartype.equals("boolean"))insts.add(0x36);
		insts.add(pc);
		int[] i = new int[insts.size()];
		for (int j = 0; j < insts.size(); j++)i[j] = insts.get(j);
		code.addCodeByte(i);
	}

	public void genFunRet(String funcname, Object v, String valueType,String retType)
    {
		if (retType.equals("void"))
        {
			this.codeMap.get(funcname).addCodeByte(new int[] { 0xb1 });// return
			// code
		}
        else if (retType.equals("const"))
        {
			if (v instanceof Integer)
            {// integer
				// ldc
				this.codeMap.get(funcname).addCodeByte(new int[] { 0x12, this.genInt((Integer) v), 0xac });
			}
            else{// real
                this.codeMap.get(funcname).addCodeByte(new int[] { 0x12, this.genFloat((Float) v), 0xae });
			}
		}
        else if (retType.equals("var"))
        {
			if (valueType.equals("real"))
            {
				this.codeMap.get(funcname).addCodeByte(
						new int[] {
								0x17,
								this.varStackStructMap.get(funcname).getVar_indexMap().get(v), 0xae });
			}
            else
            {//int
				this.codeMap.get(funcname).addCodeByte(new int[] {0x15,this.varStackStructMap.get(funcname).getVar_indexMap().get(v), 0xac });
			}
		}
		// System.out.println("\nfunction Instructions : <" + funcname
		// 		+ "> maxlocals: " + this.codeMap.get(funcname).getMax_locals()
		// 		+ " maxStacks: " + this.codeMap.get(funcname).getMax_stack());
		int cnt = 0;
		for (int i : this.codeMap.get(funcname).getCodes())
        {
			System.out.print("0x" + Integer.toHexString(i) + " ");
			cnt++;
			if (cnt >= 30)
            {
				cnt = 0;
				System.out.println();
			}
		}
		System.out.println();
	}

	public void genWrite(String funcname, ArrayList<Integer> insts,String argType)
    {
		Cp_info cp;
		if (this.constantLineNO.get("java/lang/System") == null)
        {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/lang/System".getBytes());
			this.constantLineNO.put("java/lang/System", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("SystemClass") == null)
        {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/lang/System"));
			this.constantLineNO.put("SystemClass", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("out") == null)
        {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("out".getBytes());
			this.constantLineNO.put("out", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("Ljava/io/PrintStream;") == null)
        {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("Ljava/io/PrintStream;".getBytes());
			this.constantLineNO.put("Ljava/io/PrintStream;",this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("OutIO_NameAndType") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("out"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO
							.get("Ljava/io/PrintStream;"));
			this.constantLineNO.put("OutIO_NameAndType", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("System_out_Field") == null) {
			cp = new CONSTANT_Fieldref_info();
			this.index_Constant++;
			((CONSTANT_Fieldref_info) cp).setClass_index(this.constantLineNO
					.get("SystemClass"));
			((CONSTANT_Fieldref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("OutIO_NameAndType"));
			this.constantLineNO.put("System_out_Field", this.index_Constant);
			this.addCp_info(cp);
		}

		Code code = this.codeMap.get(funcname);
		code.addCodeByte(new int[] { 0xb2 });
		short index = this.constantLineNO.get("System_out_Field");
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		int[] i = new int[insts.size()];
		for (int j = 0; j < insts.size(); j++) {
			i[j] = insts.get(j);
		}
		code.addCodeByte(i);

		// invokevirtual
		if (this.constantLineNO.get("java/io/PrintStream") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/io/PrintStream".getBytes());
			this.constantLineNO.put("java/io/PrintStream", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("PrintStreamClass") == null) {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/io/PrintStream"));
			this.constantLineNO.put("PrintStreamClass", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("print") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("print".getBytes());
			this.constantLineNO.put("print", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get(argType) == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte(argType.getBytes());
			this.constantLineNO.put(argType, this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("Println_NameAndType" + argType) == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("print"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO.get(argType));
			this.constantLineNO.put("Println_NameAndType" + argType,
					this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("PrintClass_PrintlnName" + argType) == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("PrintStreamClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("Println_NameAndType" + argType));
			this.constantLineNO.put("PrintClass_PrintlnName" + argType,
					this.index_Constant);
			this.addCp_info(cp);
		}
		// code
		index = this.constantLineNO.get("PrintClass_PrintlnName" + argType);
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { 0xb6, b[0], b[1] });
	}

	public void genRead(String funcname, String varname, String argType) {
		Cp_info cp;

		if (this.constantLineNO.get("java/util/Scanner") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/util/Scanner".getBytes());
			this.constantLineNO.put("java/util/Scanner", this.index_Constant);
			this.addCp_info(cp);
		}

		// new bb xx xx
		if (this.constantLineNO.get("ScannerClass") == null) {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/util/Scanner"));
			this.constantLineNO.put("ScannerClass", this.index_Constant);
			this.addCp_info(cp);
		}

		Code code = this.codeMap.get(funcname);
		code.addCodeByte(new int[] { 0xbb });
		short index = this.constantLineNO.get("ScannerClass");
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// dup
		code.addCodeByte(new int[] { 0x59 });

		// b2 xx xx
		if (this.constantLineNO.get("java/lang/System") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/lang/System".getBytes());
			this.constantLineNO.put("java/lang/System", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("SystemClass") == null) {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/lang/System"));
			this.constantLineNO.put("SystemClass", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("in") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("in".getBytes());
			this.constantLineNO.put("in", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("Ljava/io/InputStream;") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("Ljava/io/InputStream;"
					.getBytes());
			this.constantLineNO.put("Ljava/io/InputStream;",
					this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("INIO_NameAndType") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("in"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO
							.get("Ljava/io/InputStream;"));
			this.constantLineNO.put("INIO_NameAndType", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("System_in_Field") == null) {
			cp = new CONSTANT_Fieldref_info();
			this.index_Constant++;
			((CONSTANT_Fieldref_info) cp).setClass_index(this.constantLineNO
					.get("SystemClass"));
			((CONSTANT_Fieldref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("INIO_NameAndType"));
			this.constantLineNO.put("System_in_Field", this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb2 });
		index = this.constantLineNO.get("System_in_Field");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// invokespecial b7 xx xx
		if (this.constantLineNO.get("<init>") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("<init>".getBytes());
			this.constantLineNO.put("<init>", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("(Ljava/io/InputStream;)V") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("(Ljava/io/InputStream;)V"
					.getBytes());
			this.constantLineNO.put("(Ljava/io/InputStream;)V",
					this.index_Constant);
			this.addCp_info(cp);
		}
		if (this.constantLineNO.get("ScannerInitName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("<init>"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO
							.get("(Ljava/io/InputStream;)V"));
			this.constantLineNO.put("ScannerInitName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("ScannerInitMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("ScannerClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("ScannerInitName"));
			this.constantLineNO.put("ScannerInitMethod", this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb7 });
		index = this.constantLineNO.get("ScannerInitMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });
		// astore
		VarStack varstack = this.varStackStructMap.get(funcname);
		index = (short) varstack.getCurrentPc();
		varstack.addCurrenPc();
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { 0x3a, index, 0x19, index });// aload
		code.addMaxLocals();
		// b6 xx xx invokevisual
		if (this.constantLineNO.get("nextScanner") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("next".getBytes());
			this.constantLineNO.put("nextScanner", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("()Ljava/lang/String;") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp)
					.addByte("()Ljava/lang/String;".getBytes());
			this.constantLineNO
					.put("()Ljava/lang/String;", this.index_Constant);
			this.addCp_info(cp);
		}
		if (this.constantLineNO.get("ScannerNextName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("nextScanner"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO
							.get("()Ljava/lang/String;"));
			this.constantLineNO.put("ScannerNextName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("ScannerNextMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("ScannerClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("ScannerNextName"));
			this.constantLineNO.put("ScannerNextMethod", this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb6 });
		index = this.constantLineNO.get("ScannerNextMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// astore #index
		index = (short) varstack.getCurrentPc();
		short strIndex = index;
		varstack.addCurrenPc();
		code.addCodeByte(new int[] { 0x3a, index });
		code.addMaxLocals();

		if (varstack.getVar_indexMap().get(varname) == null) {
			index = (short) varstack.getCurrentPc();
			varstack.getVar_indexMap().put(varname, varstack.getCurrentPc());
			varstack.addCurrenPc();
		} else {
			index = (short) varstack.getVar_indexMap().get(varname).intValue();
		}

		if (argType.equals("integer")) {
			cnvtInteger(code, strIndex, index);
		} else if (argType.equals("real")) {
			cnvtFloat(code, strIndex, index);
		} else if (argType.equals("char")) {
			cnvtChar(code, strIndex, index);
		} else if (argType.equals("boolean")) {
			cnvBoolean(code, strIndex, index);
		}
	}

	private void cnvtInteger(Code code, short strIndex, short varIndex) {
		Cp_info cp;
		if (this.constantLineNO.get("java/lang/Integer") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/lang/Integer".getBytes());
			this.constantLineNO.put("java/lang/Integer", this.index_Constant);
			this.addCp_info(cp);
		}

		// new bb xx xx
		if (this.constantLineNO.get("IntegerClass") == null) {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/lang/Integer"));
			this.constantLineNO.put("IntegerClass", this.index_Constant);
			this.addCp_info(cp);
		}
		code.addCodeByte(new int[] { 0xbb });
		short index = this.constantLineNO.get("IntegerClass");
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });
		// dup
		code.addCodeByte(new int[] { 0x59 });

		// aload
		code.addCodeByte(new int[] { 0x19, strIndex });

		// invokespecial b7

		if (this.constantLineNO.get("<init>") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("<init>".getBytes());
			this.constantLineNO.put("<init>", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("(Ljava/lang/String;)V") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("(Ljava/lang/String;)V"
					.getBytes());
			this.constantLineNO.put("(Ljava/lang/String;)V",
					this.index_Constant);
			this.addCp_info(cp);
		}
		if (this.constantLineNO.get("IntegerInitName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("<init>"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO
							.get("(Ljava/lang/String;)V"));
			this.constantLineNO.put("IntegerInitName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("IntegerInitMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("IntegerClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("IntegerInitName"));
			this.constantLineNO.put("IntegerInitMethod", this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb7 });
		index = this.constantLineNO.get("IntegerInitMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// invokevirtual b6 xx xx
		if (this.constantLineNO.get("intValueInteger") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("intValue".getBytes());
			this.constantLineNO.put("intValueInteger", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("()I") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("()I".getBytes());
			this.constantLineNO.put("()I", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("IntegerIntValueName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("intValueInteger"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO.get("()I"));
			this.constantLineNO.put("IntegerIntValueName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("IntegerIntValueMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("IntegerClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("IntegerIntValueName"));
			this.constantLineNO.put("IntegerIntValueMethod",
					this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb6 });
		index = this.constantLineNO.get("IntegerIntValueMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// istore
		code.addCodeByte(new int[] { 0x36, varIndex });
		code.addMaxLocals();
	}

	private void cnvtFloat(Code code, short strIndex, short varIndex) {
		Cp_info cp;
		if (this.constantLineNO.get("java/lang/Float") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/lang/Float".getBytes());
			this.constantLineNO.put("java/lang/Float", this.index_Constant);
			this.addCp_info(cp);
		}

		// new bb xx xx
		if (this.constantLineNO.get("FloatClass") == null) {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/lang/Float"));
			this.constantLineNO.put("FloatClass", this.index_Constant);
			this.addCp_info(cp);
		}
		code.addCodeByte(new int[] { 0xbb });
		short index = this.constantLineNO.get("FloatClass");
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });
		// dup
		code.addCodeByte(new int[] { 0x59 });

		// aload
		code.addCodeByte(new int[] { 0x19, strIndex });

		// invokespecial b7
		if (this.constantLineNO.get("<init>") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("<init>".getBytes());
			this.constantLineNO.put("<init>", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("(Ljava/lang/String;)V") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("(Ljava/lang/String;)V"
					.getBytes());
			this.constantLineNO.put("(Ljava/lang/String;)V",
					this.index_Constant);
			this.addCp_info(cp);
		}
		if (this.constantLineNO.get("FloatInitName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("<init>"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO
							.get("(Ljava/lang/String;)V"));
			this.constantLineNO.put("FloatInitName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("FloatInitMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("FloatClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("FloatInitName"));
			this.constantLineNO.put("FloatInitMethod", this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb7 });
		index = this.constantLineNO.get("FloatInitMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// invokevirtual b6 xx xx
		if (this.constantLineNO.get("floatValueFloat") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("floatValue".getBytes());
			this.constantLineNO.put("floatValueFloat", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("()F") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("()F".getBytes());
			this.constantLineNO.put("()F", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("FloatfloatValueName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("floatValueFloat"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO.get("()F"));
			this.constantLineNO.put("FloatfloatValueName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("FloatfloatValueMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("FloatClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("FloatfloatValueName"));
			this.constantLineNO.put("FloatfloatValueMethod",
					this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb6 });
		index = this.constantLineNO.get("FloatfloatValueMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// fstore
		code.addCodeByte(new int[] { 0x38, varIndex });
		code.addMaxLocals();
	}

	private void cnvtChar(Code code, int strIndex, int varIndex) {
		Cp_info cp;
		// aload iconst_0
		code.addCodeByte(new int[] { 0x19, strIndex, 0x03 });
		// invokevirtual #6; //Method java/lang/String.charAt:(I)C
		// invokevirtual b6 xx xx
		if (this.constantLineNO.get("java/lang/String") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/lang/String".getBytes());
			this.constantLineNO.put("java/lang/String", this.index_Constant);
			this.addCp_info(cp);
		}
		if (this.constantLineNO.get("StringClass") == null) {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/lang/String"));
			this.constantLineNO.put("StringClass", this.index_Constant);
			this.addCp_info(cp);
		}
		if (this.constantLineNO.get("charAtString") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("charAt".getBytes());
			this.constantLineNO.put("charAtString", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("(I)C") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("(I)C".getBytes());
			this.constantLineNO.put("(I)C", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("StringCharAtName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("charAtString"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO.get("(I)C"));
			this.constantLineNO.put("StringCharAtName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("StringCharAtMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("StringClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("StringCharAtName"));
			this.constantLineNO.put("StringCharAtMethod", this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb6 });
		short index = this.constantLineNO.get("StringCharAtMethod");
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// istore
		code.addCodeByte(new int[] { 0x36, varIndex });
		code.addMaxLocals();
	}

	private void cnvBoolean(Code code, short strIndex, short varIndex) {
		Cp_info cp;
		if (this.constantLineNO.get("java/lang/Boolean") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("java/lang/Boolean".getBytes());
			this.constantLineNO.put("java/lang/Boolean", this.index_Constant);
			this.addCp_info(cp);
		}

		// new bb xx xx
		if (this.constantLineNO.get("BoolClass") == null) {
			cp = new CONSTANT_Class_info();
			this.index_Constant++;
			((CONSTANT_Class_info) cp).setName_index(this.constantLineNO
					.get("java/lang/Boolean"));
			this.constantLineNO.put("BoolClass", this.index_Constant);
			this.addCp_info(cp);
		}
		code.addCodeByte(new int[] { 0xbb });
		short index = this.constantLineNO.get("BoolClass");
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });
		// dup
		code.addCodeByte(new int[] { 0x59 });

		// aload
		code.addCodeByte(new int[] { 0x19, strIndex });

		// invokespecial b7
		if (this.constantLineNO.get("<init>") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("<init>".getBytes());
			this.constantLineNO.put("<init>", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("(Ljava/lang/String;)V") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("(Ljava/lang/String;)V"
					.getBytes());
			this.constantLineNO.put("(Ljava/lang/String;)V",
					this.index_Constant);
			this.addCp_info(cp);
		}
		if (this.constantLineNO.get("BoolInitName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("<init>"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO
							.get("(Ljava/lang/String;)V"));
			this.constantLineNO.put("BoolInitName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("BoolInitMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("BoolClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("BoolInitName"));
			this.constantLineNO.put("BoolInitMethod", this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb7 });
		index = this.constantLineNO.get("BoolInitMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// invokevirtual b6 xx xx
		if (this.constantLineNO.get("booleanValueBool") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("booleanValue".getBytes());
			this.constantLineNO.put("booleanValueBool", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("()Z") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte("()Z".getBytes());
			this.constantLineNO.put("()Z", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("BoolbooleanValueName") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get("booleanValueBool"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO.get("()Z"));
			this.constantLineNO
					.put("BoolbooleanValueName", this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("BoolbooleanValueMethod") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("BoolClass"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO
							.get("BoolbooleanValueName"));
			this.constantLineNO.put("BoolbooleanValueMethod",
					this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb6 });
		index = this.constantLineNO.get("BoolbooleanValueMethod");
		b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		// istore
		code.addCodeByte(new int[] { 0x36, varIndex });
		code.addMaxLocals();
	}

	public void genCallFuncArgs(String funcname, Object varValue,
			String varType, String type) {
		// if type=const , varValue is the const value
		// if type=?var, varValue is the var name
		Code code = this.codeMap.get(funcname);
		if (type.equals("const")) {
			int index;
			if (varType.equals("real")) {
				index = this.genFloat(new Float((String) varValue));
				// ldc #
				code.addCodeByte(new int[] { 0x12, index });
			} else {
				index = this.genInt(new Integer((String) varValue));
				// ldc
				code.addCodeByte(new int[] { 0x12, index });
			}
		} else if (type.equals("var") || type.equals("fvar"))
        {
			String varname = (String) varValue;
			VarStack vs;
			vs = this.varStackStructMap.get(funcname);
			int var_index;
			if (vs.getVar_indexMap().get(varname) == null)
            {
				System.out.println("Warning: in function '" + funcname
						+ "' variable '" + varname + "' is not INITIALIZED");
				vs.getVar_indexMap().put(varname, vs.getCurrentPc());
				var_index = vs.getCurrentPc();
				vs.addCurrenPc();
				code.addMaxLocals();
				if (varType.equals("real")) {
					// fconst0 , fstore index , fload index
					code.addCodeByte(new int[] { 0x0b, 0x38, var_index, 0x17,
							var_index });
				} else {
					// iconst0 , istore index , iload index
					code.addCodeByte(new int[] { 0x09, 0x36, var_index, 0x15,
							var_index });
				}
			} else {
				var_index = vs.getVar_indexMap().get(varname);
			}
			if (varType.equals("real")) {
				// fload
				code.addCodeByte(new int[] { 0x17, var_index });
			} else {
				// iload
				code.addCodeByte(new int[] { 0x15, var_index });
			}
		} else if (type.equals("cvar")) { // const var
			code.addCodeByte(new int[] { 0x12,
					this.constantLineNO.get(varValue) });
		}
	}

	public void genCall(String thisFunc, char ctype, String calledFunc,
			String args) {
		Code code = this.codeMap.get(thisFunc);
		// invokevirtual b8 xx xx
		Cp_info cp;
		if (this.constantLineNO.get(calledFunc + "thisClass") == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte(calledFunc.getBytes());
			this.constantLineNO.put(calledFunc + "thisClass",
					this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get(args) == null) {
			cp = new CONSTANT_Utf8_info();
			this.index_Constant++;
			((CONSTANT_Utf8_info) cp).addByte(args.getBytes());
			this.constantLineNO.put(args, this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("thisClass" + calledFunc + "Name") == null) {
			cp = new CONSTANT_NameAndType_info();
			this.index_Constant++;
			((CONSTANT_NameAndType_info) cp).setName_index(this.constantLineNO
					.get(calledFunc + "thisClass"));
			((CONSTANT_NameAndType_info) cp)
					.setDescriptor_index(this.constantLineNO.get(args));
			this.constantLineNO.put("thisClass" + calledFunc + "Name",
					this.index_Constant);
			this.addCp_info(cp);
		}

		if (this.constantLineNO.get("thisClass" + calledFunc + "Method") == null) {
			cp = new CONSTANT_Methodref_info();
			this.index_Constant++;
			((CONSTANT_Methodref_info) cp).setClass_index(this.constantLineNO
					.get("this"));
			((CONSTANT_Methodref_info) cp)
					.setName_and_type_index(this.constantLineNO.get("thisClass"
							+ calledFunc + "Name"));
			this.constantLineNO.put("thisClass" + calledFunc + "Method",
					this.index_Constant);
			this.addCp_info(cp);
		}

		code.addCodeByte(new int[] { 0xb8 });
		short index = this.constantLineNO.get("thisClass" + calledFunc
				+ "Method");
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		if (ctype == 'f') {
			// add pop
			code.addCodeByte(new int[] { 0x57 });
		}

	}

	public void removeLastCode(String funcname) {
		Code code = this.codeMap.get(funcname);
		code.removeLastCode();
	}

	private void load2Var(Code code, VarStack vs, String var2, String varType1,
			String varType2, int type2, int inc) {
		int pc;
		if (type2 == 7) {
			if (varType2.equals("integer")) {
				if (varType1.equals("real")) {
					pc = this.genFloat(new Float(var2));
					code.addCodeByte(new int[] { 0x12, pc });
					if (inc > 0) {
						// fcmpg ifge *
						code.addCodeByte(new int[] { 0x96, 0x9c, 0, 0 });
					} else {
						// fcmpl iflt *
						code.addCodeByte(new int[] { 0x95, 0x9b, 0, 0 });
					}
				} else {
					pc = this.genInt(new Integer(var2));
					code.addCodeByte(new int[] { 0x12, pc });
					// up or down
					if (inc > 0) {
						// if_icmpge
						code.addCodeByte(new int[] { 0xa2, 0, 0 });
					} else {
						// if_icmplt
						code.addCodeByte(new int[] { 0xa1, 0, 0 });

					}
				}
				this.quitLoopStack.add(code.getCode_length() - 2);
			} else {
				pc = this.genFloat(new Float(var2));
				code.addCodeByte(new int[] { 0x12, pc });
				if (inc > 0) {
					// fcmpg ifge *
					code.addCodeByte(new int[] { 0x96, 0x9c, 0, 0 });
				} else {
					// fcmpl iflt *
					code.addCodeByte(new int[] { 0x95, 0x9b, 0, 0 });
				}
				this.quitLoopStack.add(code.getCode_length() - 2);
			}
		} else if (type2 == 5 || type2 == 6) {// var
			if (vs.getVar_indexMap().get(var2) == null) {
				System.out.println("Error In for Loop:" + var2
						+ " does not Initialized.");
				System.exit(1);
			}
			// iload varindex
			if (varType2.equals("integer")) {
				code.addCodeByte(new int[] { 0x15,
						vs.getVar_indexMap().get(var2) });
				if (varType1.equals("real")) {
					code.addCodeByte(new int[] { 0x86 });
					if (inc > 0) {
						// fcmpg ifge *
						code.addCodeByte(new int[] { 0x96, 0x9c, 0, 0 });
					} else {
						// fcmpl iflt *
						code.addCodeByte(new int[] { 0x95, 0x9b, 0, 0 });
					}
				} else {
					if (inc > 0) {
						// if_icmpge
						code.addCodeByte(new int[] { 0xa2, 0, 0 });
					} else {
						// if_icmplt
						code.addCodeByte(new int[] { 0xa1, 0, 0 });

					}
				}
				this.quitLoopStack.add(code.getCode_length() - 2);

			} else if (varType2.equals("real")) {
				code.addCodeByte(new int[] { 0x17,
						vs.getVar_indexMap().get(var2) });
				if (inc > 0) {
					// fcmpg ifge *
					code.addCodeByte(new int[] { 0x96, 0x9c, 0, 0 });
				} else {
					// fcmpl iflt *
					code.addCodeByte(new int[] { 0x95, 0x9b, 0, 0 });
				}
				this.quitLoopStack.add(code.getCode_length() - 2);
			} else {
				System.out.println("<Error>In for loop " + var2
						+ " must be a integer or real");
				System.exit(1);
			}

		} else if (type2 == 2) {// cvar
			code.addCodeByte(new int[] { 0x12, this.constantLineNO.get(var2) });
			if (varType2.equals("integer")) {
				if (varType1.equals("real")) {
					code.addCodeByte(new int[] { 0x86 });// i2f
					if (inc > 0) {
						// fcmpg ifge *
						code.addCodeByte(new int[] { 0x96, 0x9c, 0, 0 });
					} else {
						// fcmpl iflt *
						code.addCodeByte(new int[] { 0x95, 0x9b, 0, 0 });
					}
				} else {
					if (inc > 0) {
						// if_icmpge
						code.addCodeByte(new int[] { 0xa2, 0, 0 });
					} else {
						// if_icmplt
						code.addCodeByte(new int[] { 0xa1, 0, 0 });

					}
				}
				this.quitLoopStack.add(code.getCode_length() - 2);
			} else {
				if (inc > 0) {
					// fcmpg ifge *
					code.addCodeByte(new int[] { 0x96, 0x9c, 0, 0 });
				} else {
					// fcmpl iflt *
					code.addCodeByte(new int[] { 0x95, 0x9b, 0, 0 });
				}
				this.quitLoopStack.add(code.getCode_length() - 2);
			}
		}
	}

	public void genForHead(String funcname, String varname, String varType,
			String var1, String varType1, int type1, String var2,
			String varType2, int type2, int inc) {
		// if type1 | type2 is 7 , var1 is a const value
		Code code = this.codeMap.get(funcname);
		VarStack vs = this.varStackStructMap.get(funcname);
		if (vs.getVar_indexMap().get(varname) == null) {
			vs.getVar_indexMap().put(varname, vs.getCurrentPc());
			vs.addCurrenPc();
			code.addMaxLocals();
		}
		int pc;
		if (type1 == 7)
        {
			if (varType1.equals("integer")) {// int
				if (varType.equals("real")) {
					pc = this.genFloat(new Float(var1));
					code.addCodeByte(new int[] { 0x12, pc, 0x38,
							vs.getVar_indexMap().get(varname), 0x17,
							vs.getVar_indexMap().get(varname) });
					this.gotoStack.add(code.getCode_length() - 2);
					varType1 = "real";
				} else {
					pc = this.genInt(new Integer(var1));
					// ldc #index istore # iload #
					code.addCodeByte(new int[] { 0x12, pc, 0x36,
							vs.getVar_indexMap().get(varname), 0x15,
							vs.getVar_indexMap().get(varname) });
					code.addMaxLocals();
					this.gotoStack.add(code.getCode_length() - 2);
					if (varType2.equals("real")) {
						code.addCodeByte(new int[] { 0x86 });// itof
					}
				}
			}
            else
            {// real
				pc = this.genFloat(new Float(var1));
				code.addCodeByte(new int[] { 0x12, pc, 0x38,
						vs.getVar_indexMap().get(varname), 0x17,
						vs.getVar_indexMap().get(varname) });
				this.gotoStack.add(code.getCode_length() - 2);
			}
		} else if (type1 == 2) {
			code.addCodeByte(new int[] { 0x12, this.constantLineNO.get(var1) });
			if (varType1.equals("integer")) {
				if (varType.equals("real")) {
					code.addCodeByte(new int[] { 0x86 });
					// fstore . fload .
					code.addCodeByte(new int[] { 0x38,
							vs.getVar_indexMap().get(varname), 0x17,
							vs.getVar_indexMap().get(varname) });
					// code.addMaxLocals();
					this.gotoStack.add(code.getCode_length() - 2);
					varType1 = "real";
				} else {
					// istore . iload .
					code.addCodeByte(new int[] { 0x36,
							vs.getVar_indexMap().get(varname), 0x15,
							vs.getVar_indexMap().get(varname) });
					code.addMaxLocals();
					this.gotoStack.add(code.getCode_length() - 2);
					if (varType2.equals("real")) {
						code.addCodeByte(new int[] { 0x86 });// i2f
					}
				}
			} else {
				// fstore . fload .
				code.addCodeByte(new int[] { 0x38,
						vs.getVar_indexMap().get(varname), 0x17,
						vs.getVar_indexMap().get(varname) });
				// code.addMaxLocals();
				this.gotoStack.add(code.getCode_length() - 2);
			}
		} else if (type1 == 5 || type1 == 6) {
			// iload * , istore var* , iload var*
			if (vs.getVar_indexMap().get(var1) == null) {
				System.out.println("Error in for loop :" + var1
						+ " is not initialized.");
				System.exit(1);
			}
			if (varType1.equals("integer")) {
				code.addCodeByte(new int[] { 0x15,
						vs.getVar_indexMap().get(var1) });
				if (varType.equals("real")) {
					code.addCodeByte(new int[] { 0x86, 0x38,
							vs.getVar_indexMap().get(varname), 0x17,
							vs.getVar_indexMap().get(varname) });
					this.gotoStack.add(code.getCode_length() - 2);
					varType1 = "real";
				} else {
					code.addCodeByte(new int[] { 0x36,
							vs.getVar_indexMap().get(varname), 0x15,
							vs.getVar_indexMap().get(varname) });
					this.gotoStack.add(code.getCode_length() - 2);
					if (varType2.equals("real")) {
						code.addCodeByte(new int[] { 0x86 });// i2f
					}
				}

			} else {
				code.addCodeByte(new int[] { 0x17,
						vs.getVar_indexMap().get(var1) });
				if (varType.equals("integer")) {
					code.addCodeByte(new int[] { 0x8b, 0x36,
							vs.getVar_indexMap().get(varname), 0x15,
							vs.getVar_indexMap().get(varname) });
				} else {
					code.addCodeByte(new int[] { 0x38,
							vs.getVar_indexMap().get(varname), 0x17,
							vs.getVar_indexMap().get(varname) });
				}
				this.gotoStack.add(code.getCode_length() - 2);
			}
		}
		load2Var(code, vs, var2, varType1, varType2, type2, inc);
	}

	public void genForTail(String funcname, int inc, String varname,
			String varType) {
		Code code = this.codeMap.get(funcname);
		VarStack vs = this.varStackStructMap.get(funcname);
		if (varType.equals("integer")) {
			// iinc *,*
			code.addCodeByte(new int[] { 0x84,
					vs.getVar_indexMap().get(varname), inc });

		} else {
			code.addCodeByte(new int[] { 0x17,
					vs.getVar_indexMap().get(varname), 0x0c });
			if (inc > 0) {
				code.addCodeByte(new int[] { 0x62, 0x38,
						vs.getVar_indexMap().get(varname) });
			} else {
				code.addCodeByte(new int[] { 0x66, 0x38,
						vs.getVar_indexMap().get(varname) });
			}
		}
		// goto
		code.addCodeByte(new int[] { 0xa7 });
		short index = (short) (code.getCode_length()
				- this.gotoStack.remove(this.gotoStack.size() - 1) - 2);
		index = (short) (0xffff - index);
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });
		code.backSetCode(this.quitLoopStack
				.remove(this.quitLoopStack.size() - 1));
	}

	public void genCondition(String funcname, String leftType,
			ArrayList<Integer> inst1, String cmptype, String rightType,
			ArrayList<Integer> inst2) {
		Code code = this.codeMap.get(funcname);
		boolean isRealCmp = false;
		if (rightType.equals("real") && leftType.equals("integer")) {
			inst1.add(0x86);// i2f
			isRealCmp = true;
		}
		if (leftType.equals("real") && rightType.equals("integer")) {
			inst2.add(0x86);
			isRealCmp = true;
		}

		if (leftType.equals("real") || rightType.equals("real")) {
			isRealCmp = true;
		}
		int[] i = new int[inst1.size()];
		for (int j = 0; j < inst1.size(); j++) {
			i[j] = inst1.get(j);
		}
		code.addCodeByte(i);
		if (inst2 != null) {
			i = new int[inst2.size()];
			for (int j = 0; j < inst2.size(); j++) {
				i[j] = inst2.get(j);
			}
			code.addCodeByte(i);
		}
		int ifcode = 0;
		// this.doWhilePc.add(code.getCode_length() - 4);
		if (!isRealCmp) { // integer cmp
			// if_icmpne if_icmpeq if_icmpge ...
			if (cmptype.equals("=")) {
				ifcode = 0xa0;
			} else if (cmptype.equals("<>")) {
				if (rightType.equals("void")) {
					code.addCodeByte(new int[] { 0x03 });
				}
				// if_icmpeq
				ifcode = 0x9f;
			} else if (cmptype.equals("<")) {
				// if_icmpge
				ifcode = 0xa2;

			} else if (cmptype.equals("<=")) {
				ifcode = 0xa3;

			} else if (cmptype.equals(">")) {
				ifcode = 0xa4;
			} else if (cmptype.equals(">=")) {
				ifcode = 0xa1;
			}
			code.addCodeByte(new int[] { ifcode, 0, 0 });
			this.quitLoopStack.add(code.getCode_length() - 2);
		} else {
			if (cmptype.equals("=")) {
				ifcode = 0x9a;
			} else if (cmptype.equals("<>")) {
				if (cmptype.equals("void")) {
					// fconst_0 fcmpl ifne *
					code.addCodeByte(new int[] { 0x0b });
				}
				ifcode = 0x99;
			} else if (cmptype.equals("<")) {
				ifcode = 0x9c;
			} else if (cmptype.equals("<=")) {
				ifcode = 0x9d;
			} else if (cmptype.equals(">")) {
				ifcode = 0x9e;
			} else if (cmptype.equals(">=")) {
				ifcode = 0x9b;
			}
			code.addCodeByte(new int[] { 0x95, ifcode, 0, 0 });
			this.quitLoopStack.add(code.getCode_length() - 2);
		}
	}

	public void genBackIf(String funcname) {
		Code code = this.codeMap.get(funcname);
		// add goto
		code.addCodeByte(new int[] { 0xa7, 0, 0 });
		this.gotoStack.add(code.getCode_length() - 2);

		code.backSetCode(this.quitLoopStack
				.remove(this.quitLoopStack.size() - 1));
	}

	public void genBackElse(String funcname) {
		Code code = this.codeMap.get(funcname);
		code.backSetCode(this.gotoStack.remove(this.gotoStack.size() - 1));
	}

	public void genWhile(String funcname) {
		Code code = this.codeMap.get(funcname);
		this.gotoStack.add(code.getCode_length());
	}

	public void genBackWhile(String funcname) {
		Code code = this.codeMap.get(funcname);
		// add goto
		code.addCodeByte(new int[] { 0xa7 });
		short index = (short) (code.getCode_length()
				- this.gotoStack.remove(this.gotoStack.size() - 1) - 2);
		index = (short) (0xffff - index);
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		code.backSetCode(this.quitLoopStack
				.remove(this.quitLoopStack.size() - 1));
	}

	public void genRepeat(String funcname) {
		Code code = this.codeMap.get(funcname);
		this.gotoStack.add(code.getCode_length());
	}

	public void genBackRepeat(String funcname) {
		Code code = this.codeMap.get(funcname);
		int back = this.gotoStack.remove(this.gotoStack.size() - 1);
		code.addCodeByte(new int[] { 0xa7, 0, 0 });
		this.gotoStack.add(code.getCode_length() - 2);
		code.backSetCode(this.quitLoopStack
				.remove(this.quitLoopStack.size() - 1));

		code.addCodeByte(new int[] { 0xa7 });

		short index = (short) (code.getCode_length() - back - 2);
		index = (short) (0xffff - index);
		byte[] b = this.short2bytes(index);
		code.addCodeByte(new int[] { b[0], b[1] });

		code.backSetCode(this.gotoStack.remove(this.gotoStack.size() - 1));

	}

}
