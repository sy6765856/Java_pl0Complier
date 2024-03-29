package parse;
import java.util.ArrayList;
import genCode.GenByteCode;
import genCode.store.VarStack;
import lexical.Lexical;
import lexical.Symbol;

public class Parsing
{
	private Lexical lexi;
	private String word;
	private GenByteCode genCode;
	private String classname;
	private String currentFuncName;
	private String currentVarType;
	private ArrayList<String> currentFuncNameStack;
	private ArrayList<String> stack_digtal;
	private ArrayList<String> stack_op;
	private ArrayList<Integer> express_inst;
	private char callType;

	public Parsing(Lexical lexi) {
		currentFuncName = "main";
		callType = 'a';
		currentFuncNameStack = new ArrayList<String>();
		this.lexi = lexi;
		genCode = new GenByteCode();
		if (syntax_check()) {
			genCode.genClassFile(this.classname);
		}
	}

	private boolean syntax_check() {
		if (!checkHead()) {
			System.out.println("Error in Header");
			return false;
		}
		return checkSubProgram(".");
	}

	public String getCurrentFuncname() {
		return this.currentFuncName;
	}

	private boolean checkSubProgram(String end) {
		while (true) {
			if (word == null) {
				System.out.println("Error<" + lexi.line + "> word is null");
				break;
			}
			if (word.equals(end)) {
				if (!checkEnd(end)) {
					return false;
				}
				break;
			}
			if (word.equals("const")) {
				if (!checkConst()) {
					System.out.println("Error in const <" + lexi.line + ">");
					return false;
				}
				continue;
			} else if (word.equals("var")) {
				if (!checkVar()) {
					System.out.println("Error in var <" + lexi.line + ">");
					return false;
				}
				if (!word.equals(";")) {
					System.out.println("Error<" + lexi.line + "> "
							+ "missing ';'");
					return false;
				}
				word = lexi.readWord();
				continue;
			} else if (word.equals("function")) {
				if (!checkFunction()) {
					System.out.println("Error in Function <" + lexi.line
							+ ">");
					return false;
				}
				continue;
			} else if (word.equals("procedure")) {
				if (!checkProcedure()) {
					System.out.println("Error in Procedure <" + lexi.line
							+ ">");
					return false;
				}
				continue;
			} else if (!checkStatement()) {
				System.out.println("Error<" + lexi.line
						+ "> in checkStatement");
				return false;
			}

		}
		return true;
	}

	private boolean checkStatement() {
		if (word.equals("call")) {
			return doCallBlock('a');
		} else if (word.equals("begin")) {
			return doBeginBlock();
		} else if (word.equals("if")) {
			return doIfBlock();
		} else if (word.equals("while")) {
			return doWhileBlock();
		} else if (word.equals("read")) {
			return doReadBlock();
		} else if (word.equals("write")) {
			return doWriteBlock();
		} else if (word.equals("for")) {
			return doForBlock();
		} else if (word.equals("repeat")) {
			return doRepeatBlock();
		} else if (!doIdentBlock()) {
			return false;
		}
		return true;
	}

	private boolean doCallBlock(char ctype) {// f - function p - procedure a
		// - all
		word = lexi.readWord();
		String funcname;
		if (lexi.isIdentity(word)) {
			Symbol sym;
			if ((sym = lexi.getSymbol(word)) != null) {
				if (!(sym.getType() == 3 || sym.getType() == 4)) {
					System.out.println("Error<" + lexi.line + "> " + word
							+ " is not a function or procedure.");
					return false;
				}
				if (ctype == 'f' && sym.getType() == 4) {
					System.out.println("Error<" + lexi.line
							+ "> procedure does not return a value");
					return false;
				} else if (sym.getType() == 3) {
					ctype = 'f';
				}
				funcname = word;
				word = lexi.readWord();
				if (!word.equals("(")) {
					System.out.println("Error<" + lexi.line
							+ "> '(' missing .");
					return false;
				}

				int i = 0;
				StringBuffer funArgs = new StringBuffer();
				funArgs.append("(");
				while (true) {
					word = lexi.readWord();
					// System.out.println(word);
					if (word.equals(")")) {
						funArgs.append(")");
						break;
					}
					if (word.equals(",")) {
						continue;
					}
					String funargsType = lexi.getFuncArgs(funcname).get(i);
					if (funargsType.equals("char")) {
						funArgs.append("C");
					} else if (funargsType.equals("integer")) {
						funArgs.append("I");
					} else if (funargsType.equals("real")) {
						funArgs.append("F");
					} else if (funargsType.equals("boolean")) {
						funArgs.append("Z");
					}
					if (lexi.isIdentity(word) && lexi.isWordMarked(word)) {
						Symbol s = lexi.getSymbol(word);
						if (s == null) {
							System.out.println("Error<" + lexi.line + "> "
									+ word + " not defined");
							return false;
						}
						String varType = s.getValueType();

						if (varType.equals("char")) {
							if (funargsType.equals("boolean")) {
								System.out.println("Error<" + lexi.line
										+ "> " + word + " is not "
										+ lexi.getFuncArgs(funcname).get(i));
								return false;
							}
						} else if (varType.equals("boolean")) {
							if (funargsType.equals("char")) {
								System.out.println("Error<" + lexi.line
										+ "> " + word + " is not "
										+ lexi.getFuncArgs(funcname).get(i));
								return false;
							}
						} else if (varType.charAt(0) > funargsType.charAt(0)) {
							System.out.println("Error<" + lexi.line + "> "
									+ word + " is not "
									+ lexi.getFuncArgs(funcname).get(i));
							return false;
						}
						// var or const type
						String type = "var";
						if (s.getType() == 2) {// const var
							type = "cvar";
						} else if (s.getType() == 5) {// var
							type = "var";
						} else if (s.getType() == 6) {// function args var
							type = "fvar";
						}
						this.genCode.genCallFuncArgs(this.currentFuncName,
								word, varType, type);

					} else if (lexi.getNextChar() == '\''
							&& lexi.isChar(word)) {
						if (funargsType.equals("boolean")) {
							System.out.println("Error<" + lexi.line + "> "
									+ word + " is not "
									+ lexi.getFuncArgs(funcname).get(i));
							return false;
						}
						// char args const
						Integer ic = (int) word.charAt(0);
						this.genCode.genCallFuncArgs(this.currentFuncName, ic
								.toString(), "char", "const");

					} else if (lexi.isBool(word)) {
						if (funargsType.equals("char")) {
							System.out.println("Error<" + lexi.line + "> "
									+ word + " is not "
									+ lexi.getFuncArgs(funcname).get(i));
							return false;
						}
						// boolean args const
						if (word.equals("true")) {
							this.genCode.genCallFuncArgs(this.currentFuncName,
									"1", "boolean", "const");
						} else {
							this.genCode.genCallFuncArgs(this.currentFuncName,
									"0", "boolean", "const");
						}

					} else if (lexi.isInt(word)) {
						if (funargsType.equals("integer")) {
							this.genCode.genCallFuncArgs(this.currentFuncName,
									word, "integer", "const");
						} else if (funargsType.equals("real")) {
							this.genCode.genCallFuncArgs(this.currentFuncName,
									word, "real", "const");
						} else {
							System.out.println("Error<" + lexi.line + "> "
									+ word + " is not "
									+ lexi.getFuncArgs(funcname).get(i));
							return false;
						}
					} else if (lexi.isFloat(word)) {
						if (!funargsType.equals("real")) {
							System.out.println("Error<" + lexi.line + "> "
									+ word + " is not "
									+ lexi.getFuncArgs(funcname).get(i));
							return false;
						}
						// real args const
						this.genCode.genCallFuncArgs(this.currentFuncName,
								word, "real", "const");
					} else {
						System.out.println("Error<" + lexi.line
								+ "unknow function args type.");
						return false;
					}
					i++;
				}
				sym = lexi.getSymbol(funcname);
				if (sym.getValueType().equals("char")) {
					funArgs.append("C");
				} else if (sym.getValueType().equals("integer")) {
					funArgs.append("I");
				} else if (sym.getValueType().equals("real")) {
					funArgs.append("F");
				} else if (sym.getValueType().equals("boolean")) {
					funArgs.append("Z");
				} else {
					funArgs.append("V");
				}
				this.genCode.genCall(this.currentFuncName, ctype, funcname,
						funArgs.toString());
			} else {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " function does not define.");
				return false;
			}
		}
		word = lexi.readWord();
		return true;
	}

	private boolean doBeginBlock() {
		word = lexi.readWord();
		if (!this.checkStatement()) {
			return false;
		}
		while (word.equals(";")) {
			word = lexi.readWord();
			checkStatement();
		}
		if (!word.equals("end")) {
			System.out.println("Error<" + lexi.line
					+ "> missing 'end' in this begin block.");
			return false;
		}
		word = lexi.readWord();
		return true;
	}

	private boolean doIfBlock() {
		if (!this.doCondition()) {
			return false;
		}
		if (!word.equals("then")) {
			System.out.println("Error<" + lexi.line + "> missing 'then' .");
			return false;
		}
		word = lexi.readWord();
		if (!this.checkStatement()) {
			return false;
		}
		this.genCode.genBackIf(this.currentFuncName);
		if (word.equals("else")) {
			word = lexi.readWord();
			if (!this.checkStatement()) {
				return false;
			}
		}
		this.genCode.genBackElse(this.currentFuncName);
		return true;
	}

	private boolean doWhileBlock() {
		this.genCode.genWhile(this.currentFuncName);
		if (!this.doCondition()) {
			return false;
		}
		if (!word.equals("do")) {
			System.out.println("Error<" + lexi.line + "> missing 'do' .");
			return false;
		}
		word = lexi.readWord();
		if (!this.checkStatement()) {
			return false;
		}
		this.genCode.genBackWhile(this.currentFuncName);
		return true;
	}

	private boolean doReadBlock() {
		word = lexi.readWord();
		if (!word.equals("(")) {
			System.out.println("Error<" + lexi.line
					+ "> missing '(' after read .");
			return false;
		}
		word = lexi.readWord();
		while (true) {
			Symbol s;
			if (!lexi.isWordMarked(word)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " undefined.");
				return false;
			} else {
				s = lexi.getSymbol(word);
				if (s.getType() == 2) {
					System.out.println("Error<" + lexi.line + "> " + word
							+ " is a const , can not be assigned value.");
					return false;
				} else if (!(s.getType() == 5 || s.getType() == 6)) {
					System.out.println("Error<" + lexi.line + "> " + word
							+ " is not a variable .");
					return false;
				}
				word = lexi.readWord();
			}
			this.genCode.genRead(this.currentFuncName, s.getName(), s
					.getValueType());
			if (word.equals(",")) {
				word = lexi.readWord();
				continue;
			} else {
				break;
			}
		}
		if (!word.equals(")")) {
			System.out.println("Error<" + lexi.line
					+ "> missing ')' after read .");
			return false;
		}
		word = lexi.readWord();
		return true;
	}

	private boolean doWriteBlock() {
		word = lexi.readWord();
		if (!word.equals("(")) {
			System.out
					.println("Error<" + lexi.line + "> " + "missing '(' .");
			return false;
		}
		String argType = "()V";
		while (true) {
			// enter expression
			express_inst = new ArrayList<Integer>();
			stack_digtal = new ArrayList<String>();
			stack_op = new ArrayList<String>();
			stack_op.add("#");
			stack_digtal.add("#");
			if (!doExpress()) {
				return false;
			}

			argType = stack_digtal.get(stack_digtal.size() - 1);
			// System.out.println("write block : args type is " + argType);
			if (argType.equals("real")) {
				argType = "(F)V";
			} else if (argType.equals("integer")) {
				argType = "(I)V";
			} else if (argType.equals("char")) {
				argType = "(C)V";
			} else if (argType.equals("boolean")) {
				argType = "(Z)V";
			}
			this.genCode.genWrite(this.currentFuncName, express_inst, argType);

			if (word.equals(",")) {
				continue;
			} else {
				break;
			}
		}
		if (!word.equals(")")) {
			System.out
					.println("Error<" + lexi.line + "> " + "missing ')' .");
			return false;
		}
		word = lexi.readWord();
		return true;
	}

	private boolean doExpress() {
		word = lexi.readWord();
		if (word.equals("odd") || word.equals("call")) {
			return false;
		}
		if (word.equals("+") || word.equals("-")) {
			stack_op.add(word);
			word = lexi.readWord();
		}
		if (!doItem(stack_digtal, stack_op)) {
			System.out.println("Error<" + lexi.line + "> Express.");
			return false;
		}
		while (true) {
			if (!(word.equals("+") || word.equals("-"))) {
				break;
			} else {
				if (cmp_op(word) == 1) {
					stack_op.add(word);
				} else if (cmp_op(word) == 0) {
					if (!((stack_op.get(stack_op.size() - 1).equals("#")) || (stack_op
							.get(stack_op.size() - 1).equals("(")))) {
						if (stack_digtal.size() <= 2) {
							if (!operate(stack_op.remove(stack_op.size() - 1),
									null, stack_digtal.remove(stack_digtal
											.size() - 1))) {
								return false;
							}
						} else {
							String o = stack_digtal
									.remove(stack_digtal.size() - 1);
							if (!operate(stack_op.remove(stack_op.size() - 1),
									stack_digtal
											.remove(stack_digtal.size() - 1), o)) {
								return false;
							}
						}
					}
					stack_op.add(word);
				}
			}
			word = lexi.readWord();

			if (!doItem(stack_digtal, stack_op)) {
				return false;
			}
		}
		while (true) {
			if (!stack_op.get(stack_op.size() - 1).equals("#")) {
				if (stack_op.get(stack_op.size() - 1).equals("(")) {
					if (word.equals(")")) {
						stack_op.remove(stack_op.size() - 1);
						break;
					} else {
						System.out.println("Error<" + lexi.line + ">");
						return false;
					}
				}
				if (stack_digtal.size() <= 2) {
					if (!operate(stack_op.remove(stack_op.size() - 1), null,
							stack_digtal.remove(stack_digtal.size() - 1))) {
						return false;
					}
				} else {
					String o = stack_digtal.remove(stack_digtal.size() - 1);
					if (!operate(stack_op.remove(stack_op.size() - 1),
							stack_digtal.remove(stack_digtal.size() - 1), o)) {
						return false;
					}
				}
			} else {
				break;
			}
		}
		return true;
	}

	private boolean doItem(ArrayList<String> stack_digtal,
			ArrayList<String> stack_op) {
		if (!doFactor(stack_digtal, stack_op)) {
			System.out.println("Error<" + lexi.line + "> in do factor");
			return false;
		}
		while (true) {
			if (word.equals("*") || word.equals("/")) {
				if (cmp_op(word) == 1) {// >
					stack_op.add(word);
				} else if (cmp_op(word) == 0) {// <
					if (!stack_op.get(stack_op.size() - 1).equals("#")) {
						if (stack_digtal.size() <= 2) {
							if (!operate(stack_op.remove(stack_op.size() - 1),
									null, stack_digtal.remove(stack_digtal
											.size() - 1))) {
								return false;
							}
						} else {
							String o = stack_digtal
									.remove(stack_digtal.size() - 1);
							if (!operate(stack_op.remove(stack_op.size() - 1),
									stack_digtal
											.remove(stack_digtal.size() - 1), o)) {
								return false;
							}
						}
					}
					stack_op.add(word);
				}
				word = lexi.readWord();
				if (!doFactor(stack_digtal, stack_op)) {
					return false;
				}
				// System.out.println(word);
			} else {
				break;
			}
		}
		return true;
	}

	private boolean doFactor(ArrayList<String> stack_digtal,
			ArrayList<String> stack_op) {
		if (lexi.isIdentity(word) && (lexi.getSymbol(word) != null)
				&& (lexi.getNextChar() != '\'')) {
			Symbol symbol = lexi.getSymbol(word);
			if (!(symbol.getType() == 2 || symbol.getType() == 5 || symbol
					.getType() == 6)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ "is not a varliable.");
				return false;
			} else {
				stack_digtal.add(symbol.getValueType());
				if (symbol.getType() == 6)
                {
					if (symbol.getValueType().equals("integer")
							|| symbol.getValueType().equals("char")
							|| symbol.getValueType().equals("boolean")) {
						// iload stack_index
						express_inst.add(0x15);
						express_inst.add((Integer) symbol.getValue());
						if (this.stack_digtal.get(stack_digtal.size() - 2)
								.equals("real")
								|| (this.currentVarType != null && this.currentVarType
										.equals("real"))) {
							express_inst.add(0x86);
							stack_digtal.remove(stack_digtal.size() - 1);
							stack_digtal.add("real");
						}
					} else if (symbol.getValueType().equals("real")) {
						// fload
						if (!this.stack_digtal.get(stack_digtal.size() - 2)
								.equals("real")
								&& !this.stack_digtal.get(
										stack_digtal.size() - 2).equals("#")) {
							express_inst.add(0x86);
						}
						express_inst.add(0x17);
						express_inst.add((Integer) symbol.getValue());
					} else {
						System.out.println("Error<" + lexi.line
								+ "> unComputable value here.");
						return false;
					}
				}
                else
                {//var
					if (symbol.getType() == 2)
                    { //const ldc #index_Constant
						if (symbol.getValueType().equals("real"))
                        {
							if (!this.stack_digtal.get(stack_digtal.size() - 2)
									.equals("real")
									&& !this.stack_digtal.get(
											stack_digtal.size() - 2)
											.equals("#")) {
								express_inst.add(0x86);
							}
						}
						express_inst.add(0x12);
						express_inst.add((int) this.genCode.getConstantLineNO()
								.get(symbol.getName()));
						if (!symbol.getValueType().equals("real")) {
							if (this.stack_digtal.get(stack_digtal.size() - 2)
									.equals("real")
									|| (this.currentVarType != null && this.currentVarType
											.equals("real"))) {
								express_inst.add(0x86);
								stack_digtal.remove(stack_digtal.size() - 1);
								stack_digtal.add("real");
							}
						}
					}
                    else if (symbol.getType() == 5)//var
                    {
						if (this.genCode.getStackMap()
								.get(this.currentFuncName).getVar_indexMap()
								.get(symbol.getName()) == null) {
							System.out
									.println("Error<" + lexi.line + "> "
											+ symbol.getName()
											+ " is not initialized.");
							return false;
						}
						int pc = this.genCode.getStackMap().get(
								this.currentFuncName).getVar_indexMap().get(symbol.getName());
						if (symbol.getValueType().equals("real")) {
							// fload
							if (!this.stack_digtal.get(stack_digtal.size() - 2)
									.equals("real")
									&& !this.stack_digtal.get(
											stack_digtal.size() - 2)
											.equals("#")) {
								express_inst.add(0x86);
							}
							express_inst.add(0x17);
							express_inst.add(pc);
						} else if (symbol.getValueType().equals("integer")) {
							// iload
							express_inst.add(0x15);
							express_inst.add(pc);
							if (this.stack_digtal.get(stack_digtal.size() - 2)
									.equals("real")
									|| (this.currentVarType != null && this.currentVarType
											.equals("real"))) {
								express_inst.add(0x86);
								stack_digtal.remove(stack_digtal.size() - 1);
								stack_digtal.add("real");
							}
						} else if (symbol.getValueType().equals("char")) {
							express_inst.add(0x15);
							express_inst.add(pc);
							if (this.stack_digtal.get(stack_digtal.size() - 2)
									.equals("real")
									|| (this.currentVarType != null && this.currentVarType
											.equals("real"))) {
								express_inst.add(0x86);
								stack_digtal.remove(stack_digtal.size() - 1);
								stack_digtal.add("real");
							}
						} else if (symbol.getValueType().equals("boolean")) {
							express_inst.add(0x15);
							express_inst.add(pc);
							if (this.stack_digtal.get(stack_digtal.size() - 2)
									.equals("real")
									|| (this.currentVarType != null && this.currentVarType
											.equals("real"))) {
								express_inst.add(0x86);
								stack_digtal.remove(stack_digtal.size() - 1);
								stack_digtal.add("real");
							}
							// express_inst.add(0x);
						} else {
							System.out.println("Error<" + lexi.line + "> "
									+ symbol.getName() + " not allowed here.");
							return false;
						}
					}
				}
			}
		} else if (lexi.getNextChar() == '\'' && lexi.isChar(word)) {
			stack_digtal.add("integer");
			express_inst.add(0x12);
			express_inst.add(this.genCode.genInt(new Integer(word.charAt(0))));
			if (this.stack_digtal.get(stack_digtal.size() - 2).equals("real")
					|| (this.currentVarType != null && this.currentVarType
							.equals("real"))) {
				express_inst.add(0x86);
				stack_digtal.remove(stack_digtal.size() - 1);
				stack_digtal.add("real");
			}
		} else if (lexi.isInt(word)) {
			stack_digtal.add("integer");
			express_inst.add(0x12);
			express_inst.add(this.genCode.genInt(new Integer(word)));
			if (this.stack_digtal.get(stack_digtal.size() - 2).equals("real")
					|| (this.currentVarType != null && this.currentVarType
							.equals("real"))) {
				express_inst.add(0x86);
				stack_digtal.remove(stack_digtal.size() - 1);
				stack_digtal.add("real");
			}
		} else if (lexi.isFloat(word)) {
			stack_digtal.add("real");
			if (!this.stack_digtal.get(stack_digtal.size() - 2).equals("real")
					&& !this.stack_digtal.get(stack_digtal.size() - 2).equals(
							"#")) {
				express_inst.add(0x86);
			}
			express_inst.add(0x12);
			express_inst.add(this.genCode.genFloat(new Float(word)));
		} else if (lexi.isBool(word)) {
			stack_digtal.add("integer");
			if (word.equals("false")) {
				express_inst.add(0x3);// iconst_0
			} else {
				express_inst.add(0x4);
			}
			if (this.stack_digtal.get(stack_digtal.size() - 2).equals("real")
					|| (this.currentVarType != null && this.currentVarType
							.equals("real"))) {
				express_inst.add(0x86);
				stack_digtal.remove(stack_digtal.size() - 1);
				stack_digtal.add("real");
			}
		} else if (word.equals("(")) {
			stack_op.add(word);
			if (!this.doExpress()) {
				return false;
			}
			if (!word.equals(")")) {
				System.out.println("Error<" + lexi.line + "> missing ')' .");
				return false;
			} else {
				if (stack_op.get(stack_op.size() - 1).equals("(")) {
					stack_op.remove(stack_op.size() - 1);
				}
			}
		} else {
			return false;
		}
		word = lexi.readWord();
		return true;
	}

	private int cmp_op(String op)
    {
		if (op.equals("(")) {
			return 1;
		}
		if (op.equals(")")) {
			return 0;
		}
		String tmp = stack_op.get(stack_op.size() - 1);
		if ((op.equals("+") || op.equals("-"))
				&& (tmp.equals("+") || tmp.equals("-") || tmp.equals("#") || tmp
						.equals(")"))) {
			return 1;
		} else if ((op.equals("+") || op.equals("-"))
				&& (tmp.equals("*") || tmp.equals("/") || tmp.equals("("))) {
			return 0;
		} else if (op.equals("*") || op.equals("/")) {
			return 1;
		} else {
			System.out.println("Error<" + lexi.line
					+ "> operator compare error.");
			return -1;
		}
	}

	private boolean operate(String op, String v1, String v2) {
		String type;
		if (v1 == null) {
			if (v2.equals("integer") || v2.equals("char")) {
				type = "integer";
			} else {
				type = "real";
			}
		} else {
			if ((v1.equals("integer") || v1.equals("char"))
					&& (v2.equals("integer") || v2.equals("char"))) {
				type = "integer";
			} else {
				type = "real";
			}
		}
		stack_digtal.add(type);
		if (op.equals("+")) {
			if (type.equals("integer")) {
				if (v1 != null) {
					this.express_inst.add(0x60);// iadd
				}
				if (this.currentVarType != null
						&& this.currentVarType.equals("real")) {
					this.express_inst.add(0x86);
				}

			} else {
				if (v1 != null) {
					this.express_inst.add(0x62);// fadd
				}
				if (this.currentVarType != null
						&& this.currentVarType.equals("integer")) {
					this.express_inst.add(0x8b);
				}
			}
		} else if (op.equals("-")) {
			if (type.equals("integer")) {
				// isub
				if (v1 != null) {
					this.express_inst.add(0x64);
				} else {
					this.express_inst.add(0x74);
				}
				if (this.currentVarType != null
						&& this.currentVarType.equals("real")) {
					this.express_inst.add(0x86);
				}
			} else {
				// fsub
				if (v1 != null) {
					this.express_inst.add(0x66);
				} else {
					this.express_inst.add(0x76);
				}
				if (this.currentVarType != null
						&& this.currentVarType.equals("integer")) {
					this.express_inst.add(0x8b);
				}
			}
		} else if (op.equals("*")) {
			if (type.equals("integer")) {
				// imul
				this.express_inst.add(0x68);
				if (this.currentVarType != null
						&& this.currentVarType.equals("real")) {
					this.express_inst.add(0x86);
				}
			} else {
				// fmul
				this.express_inst.add(0x6a);
				if (this.currentVarType != null
						&& this.currentVarType.equals("integer")) {
					this.express_inst.add(0x8b);
				}
			}
		} else if (op.equals("/")) {
			if (type.equals("integer")) {
				// idiv
				this.express_inst.add(0x6c);
				if (this.currentVarType != null
						&& this.currentVarType.equals("real")) {
					this.express_inst.add(0x86);
				}
			} else {
				// fdiv
				this.express_inst.add(0x6e);
				if (this.currentVarType != null
						&& this.currentVarType.equals("integer")) {
					this.express_inst.add(0x8b);
				}
			}
		} else {
			System.out.println("Error<" + lexi.line + "> " + op
					+ " is not operator.");
			return false;
		}
		return true;
	}

	private boolean doForBlock() {
		word = lexi.readWord();
		if (!lexi.isWordMarked(word)) {
			System.out.println("Error<" + lexi.line + "> " + word
					+ " undefined.");
			return false;
		} else {
			if (!(lexi.getSymbol(word).getType() == 5 || lexi.getSymbol(
					word).getType() == 6)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " must be a variable.");
				return false;
			}
			if (!(lexi.getSymbol(word).getValueType().equals("integer") || lexi
					.getSymbol(word).getValueType().equals("real"))) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " illege type here.");
				return false;
			}
		}
		String varname = word;
		String varType = lexi.getSymbol(word).getValueType();
		word = lexi.readWord();
		if (!word.equals(":=")) {
			System.out.println("Error<" + lexi.line + "> "
					+ "':=' missing .");
			return false;
		}
		int type1 = 7;
		String varType1 = null;
		String varValue1 = null;
		word = lexi.readWord();
		Symbol symbol = lexi.getSymbol(word);
		if (symbol != null) {
			type1 = symbol.getType();
			if (!(type1 == 2 || type1 == 5 || type1 == 6)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " not allowed here.");
				return false;
			}
			varType1 = symbol.getValueType();
			if (!(varType1.equals("integer") || varType1.equals("real"))) {
				System.out
						.println("Error<"
								+ lexi.line
								+ ">"
								+ word
								+ " not allowed here . only integer and real is allowed");
				return false;
			}
			varValue1 = word;
		} else if (lexi.isInt(word)) {
			varValue1 = word;
			varType1 = "integer";
		} else if ((lexi.isFloat(word))) {
			varValue1 = word;
			varType1 = "real";
		} else {
			System.out.println("Error<" + lexi.line + "> " + word
					+ " not allowed here.");
			return false;
		}
		word = lexi.readWord();
		int inc;
		if (word.equals("to")) {
			inc = 1;
		} else if (word.equals("downto")) {
			inc = -1;
		} else {
			System.out.println("Error<" + lexi.line + "> "
					+ "to or downto is missing .");
			return false;
		}
		int type2 = 7;
		String varType2;
		String varValue2;
		word = lexi.readWord();
		symbol = lexi.getSymbol(word);
		if (symbol != null) {
			type2 = symbol.getType();// var const functionVar
			if (!(type2 == 2 || type2 == 5 || type2 == 6)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " not allowed here.");
				return false;
			}
			varType2 = symbol.getValueType();
			if (!(varType2.equals("integer") || varType2.equals("real"))) {
				System.out
						.println("Error<"
								+ lexi.line
								+ ">"
								+ word
								+ " not allowed here . only integer and real is allowed");
				return false;
			}
			varValue2 = word;
		} else if (lexi.isInt(word)) {
			varValue2 = word;
			varType2 = "integer";
		} else if ((lexi.isFloat(word))) {
			varValue2 = word;
			varType2 = "real";
		} else {
			System.out.println("Error<" + lexi.line + "> " + word
					+ " not allowed here.");
			return false;
		}
		if (varType.equals("integer") && varType1.equals("real")) {
			System.out.println("Error<" + lexi.line
					+ "> assign real type to integer variable");
			return false;
		}
		this.genCode.genForHead(this.currentFuncName, varname, varType,
				varValue1, varType1, type1, varValue2, varType2, type2, inc);
		word = lexi.readWord();
		if (!word.equals("do")) {
			System.out.println("Error<" + lexi.line + "> "
					+ "'do' is missing .");
			return false;
		}
		word = lexi.readWord();
		if (!this.checkStatement()) {
			return false;
		}
		if (!word.equals(";")) {
			System.out.println("Error<" + lexi.line + "> ';' is missing");
			return false;
		}

		this.genCode.genForTail(this.currentFuncName, inc, varname, varType);
		word = lexi.readWord();
		return true;
	}

	private boolean doRepeatBlock() {
		word = lexi.readWord();
		this.genCode.genRepeat(this.currentFuncName);
		while (!word.equals("until")) {
			if (!this.checkStatement()) {
				return false;
			}
		}
		if (!doCondition()) {
			return false;
		}
		this.genCode.genBackRepeat(this.currentFuncName);
		return true;
	}

	private boolean doCondition() {
		express_inst = new ArrayList<Integer>();
		stack_digtal = new ArrayList<String>();
		stack_op = new ArrayList<String>();
		stack_op.add("#");
		stack_digtal.add("#");
		String leftType;
		if (!this.doExpress()) {
			if (word.equals("odd")) {
				if (!this.doExpress()) {
					return false;
				}
				leftType = this.stack_digtal.get(this.stack_digtal.size() - 1);
				this.genCode.genCondition(this.currentFuncName, leftType,
						express_inst, "<>", "void", null);
				return true;
			} else {
				return false;
			}
		}
		leftType = this.stack_digtal.get(this.stack_digtal.size() - 1);
		String cmptype;
		if (!(word.equals("=") || word.equals("<>") || word.equals("<")
				|| word.equals("<=") || word.equals(">") || word.equals(">="))) {
			System.out.println("Error<" + lexi.line
					+ "> missing Comparison operators");
			return false;
		}
		cmptype = word;
		ArrayList<Integer> inst1 = this.express_inst;
		String rightType;
		express_inst = new ArrayList<Integer>();
		stack_digtal = new ArrayList<String>();
		stack_op = new ArrayList<String>();
		stack_op.add("#");
		stack_digtal.add("#");
		if (!this.doExpress()) {
			return false;
		}
		rightType = this.stack_digtal.get(this.stack_digtal.size() - 1);
		this.genCode.genCondition(this.currentFuncName, leftType, inst1,
				cmptype, rightType, this.express_inst);
		return true;
	}

	private boolean doIdentBlock() {
		String varname;
		if (!lexi.isWordMarked(word)) {
			System.out.println("Error<" + lexi.line + "> " + word
					+ " undefined");
			return false;
		} else {
			if (!(lexi.getSymbol(word).getType() == 5 || lexi.getSymbol(
					word).getType() == 6)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " is not a variable .");
				return false;
			}
		}
		varname = word;
		word = lexi.readWord();
		if (!word.equals(":=")) {
			System.out.println("Error<" + lexi.line + "> missing ':='");
			return false;
		}
		express_inst = new ArrayList<Integer>();
		stack_digtal = new ArrayList<String>();
		stack_op = new ArrayList<String>();
		stack_op.add("#");
		stack_digtal.add("#");
		this.currentVarType = lexi.getSymbol(varname).getValueType();
		if (!this.doExpress()) {
			if (word.equals("call"))
            {
				if (!this.doCallBlock('f'))
                {
					return false;
				} else {
					// remove pop
					this.genCode.removeLastCode(this.currentFuncName);
				}
			} else {
				return false;
			}
		}
		this.genCode.genAssignValue(this.currentFuncName, varname,
				this.express_inst, this.currentVarType);
		this.currentVarType = null;
		// lexi.setSymbolValue(varname, this.express_inst);
		return true;
	}

	private boolean checkVar() {
		Symbol s = new Symbol();
		word = lexi.readWord();
		if (!lexi.isIdentity(word)) {
			System.out.println("Error<" + lexi.line + "> "
					+ "var followed by a identity");
			return false;
		}
		if (lexi.isWordMarked(word)) {
			System.out.println("Error<" + lexi.line + "> " + word
					+ " already exist! choose another.");
			return false;
		}
		s.setName(word);
		s.setType(5);
		word = lexi.readWord();
		if (!word.equals(":")) {
			System.out.println("Error<" + lexi.line + "> " + "missing ':'");
			return false;
		}
		word = lexi.readWord();
		if (!(word.equals("char") || word.equals("integer")
				|| word.equals("real") || word.equals("boolean"))) {
			System.out.println("Error<" + lexi.line + "> "
					+ "var must be char or integer or real or boolean");
			return false;
		}
		s.setValueType(word);
		s.setValue(0);
		lexi.addElem2Symbol(s);
		word = lexi.readWord();
		if (word.equals(",")) {
			checkVar();
		}
		return true;
	}

	private boolean checkFunction() {
		this.callType = 'f';
		Symbol symbol = new Symbol();
		word = lexi.readWord();
		if (!lexi.isIdentity(word)) {
			System.out.println("Error<" + lexi.line + "> " + word
					+ " is not a identity.");
			return false;
		}
		this.currentFuncNameStack.add(this.currentFuncName);
		this.currentFuncName = word;
		symbol.setName(word);
		symbol.setType(3);
		this.genCode.getStackMap().put(word, new VarStack());
		word = lexi.readWord();
		if (!word.equals("(")) {
			System.out.println("Error<" + lexi.line + "> '(' is missing .");
			return false;
		}
		if (!doArgs(symbol.getName())) {
			return false;
		}
		if (!word.equals(")")) {
			System.out.println("Error<" + lexi.line + "> ')' is missing .");
			return false;
		}
		word = lexi.readWord();
		if (!word.equals(":")) {
			System.out.println("Error<" + lexi.line + "> missing ':'");
			return false;
		}
		word = lexi.readWord();
		if (!(word.equals("char") || word.equals("integer")
				|| word.equals("real") || word.equals("boolean"))) {
			System.out
					.println("Error<"
							+ lexi.line
							+ "> function return value type is char | integer | real | boolean");
			return false;
		}
		symbol.setValueType(word);
		symbol.setValue(null);
		this.lexi.addElem2Symbol(symbol);
		word = lexi.readWord();
		if (!word.equals(";")) {
			System.out.println("Error<" + lexi.line + "> ';' is missing .");
			return false;
		}
		// generate code
		this.genCode.genFun(symbol.getName(), genArgs(symbol.getName(), symbol
				.getValueType()), (short) lexi.getFuncArgs(
				this.currentFuncName).size());
		word = lexi.readWord();
		if (!this.checkSubProgram(";")) {
			return false;
		}
		word = lexi.readWord();
		if (!word.equals("return")) {
			System.out.println("Error<" + lexi.line
					+ "> function lack of return statement");
			return false;
		}
		word = lexi.readWord();
		String retType = "const";
		Object value = null;
		String valueType = null;
		String orgType = null;
		char c = lexi.getNextChar();
		if (c == '\'' && lexi.isChar(word)) {
			orgType = "char";
			value = new Integer(word.charAt(0));
			valueType = "integer";
		} else if (lexi.isBool(word)) {
			orgType = "boolean";
			if (word.equals("true")) {
				value = new Integer(1);
			} else {
				value = new Integer(0);
			}
			valueType = "integer";
		} else if (lexi.isIdentity(word)) {
			if (lexi.isWordMarked(word)) {
				valueType = lexi.getSymbol(word).getValueType();
				if (valueType.equals("boolean")) {
					valueType = "integer";
				} else if (valueType.equals("char")) {
					valueType = "integer";
					orgType = "char";
				} else if (valueType.equals("integer")) {
					orgType = "integer";
				} else {
					orgType = "real";
				}
				value = word;
				retType = "var";
			} else {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " is not defined");
				return false;
			}
		} else if (lexi.isInt(word)) {
			value = new Integer(word);
			orgType = "integer";
			valueType = "integer";
		} else if (lexi.isFloat(word)) {
			orgType = "real";
			value = new Float(word);
			valueType = "real";
		} else {
			System.out.println("<Error" + lexi.line
					+ "> Unknow return value type '" + word + "'");
			return false;
		}
		// check return type
		if (orgType.equals(symbol.getValueType())) {

		} else {
			System.out.println("Error<" + lexi.line + "> function "
					+ this.currentFuncName + " return type uncomparable.");
			return false;
		}
		// function return code;
		this.genCode.genFunRet(this.currentFuncName, value, valueType, retType);
		word = lexi.readWord();
		this.currentFuncName = this.currentFuncNameStack
				.get(this.currentFuncNameStack.size() - 1);
		this.callType = 'a';
		return true;
	}

	private String genArgs(String funcname, String reType) {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> args;
		sb.append("(");
		args = lexi.getFuncArgs(funcname);
		if (args != null) {
			for (String s : args) {
				if (s.equals("integer")) {
					sb.append("I");
				} else if (s.equals("char")) {
					sb.append("C");
				} else if (s.equals("real")) {
					sb.append("F");
				} else if (s.equals("boolean")) {
					sb.append("Z");
				}
			}
		}
		sb.append(")");
		if (reType.equals("integer")) {
			sb.append("I");
		} else if (reType.equals("char")) {
			sb.append("C");
		} else if (reType.equals("real")) {
			sb.append("F");
		} else if (reType.equals("boolean")) {
			sb.append("Z");
		} else {
			sb.append("V");
		}
		return sb.toString();
	}

	private boolean doArgs(String funcname) {
		ArrayList<String> args = new ArrayList<String>();
		Symbol s;
		int index = 0;
		do {
			s = new Symbol();
			word = lexi.readWord();
			if (word.equals(")")) {
				break;
			}
			if (!word.equals("var")) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " illege here.");
				return false;
			}
			word = lexi.readWord();
			if (!lexi.isIdentity(word)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " is not a identity.");
				return false;
			}
			this.genCode.getStackMap().get(this.currentFuncName)
					.getVar_indexMap().put(word, index);
			this.genCode.getStackMap().get(this.currentFuncName).addCurrenPc();
			s.setName(word);
			s.setType(6);
			word = lexi.readWord();
			if (!word.equals(":")) {
				System.out.println("Error<" + lexi.line + "> missing ':'");
				return false;
			}
			word = lexi.readWord();
			if (!(word.equals("char") || word.equals("integer")
					|| word.equals("real") || word.equals("boolean"))) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " must be char | integer | real | boolean");
				return false;
			}
			s.setValueType(word);
			s.setValue(index);
			index++;
			this.lexi.addElem2Symbol(s);
			args.add(word);
			word = lexi.readWord();
		} while (word.equals(","));
		lexi.addFuncArgs(funcname, args);
		return true;
	}

	private boolean checkProcedure() {
		Symbol symbol = new Symbol();
		word = lexi.readWord();
		if (!lexi.isIdentity(word)) {
			System.out.println("Error<" + lexi.line + "> " + word
					+ " is not a identity.");
			return false;
		}
		this.currentFuncNameStack.add(this.currentFuncName);
		this.currentFuncName = word;
		this.genCode.getStackMap().put(word, new VarStack());
		symbol.setName(word);
		symbol.setType(4);
		symbol.setValueType("void");
		symbol.setValue(null);
		this.lexi.addElem2Symbol(symbol);
		word = lexi.readWord();
		if (!word.equals("(")) {
			System.out.println("Error<" + lexi.line + "> '(' is missing .");
			return false;
		}
		if (!doArgs(symbol.getName())) {
			return false;
		}
		if (!word.equals(")")) {
			System.out.println("Error<" + lexi.line + "> ')' is missing .");
			return false;
		}
		word = lexi.readWord();
		if (!word.equals(";")) {
			System.out.println("Error<" + lexi.line + "> ';' is missing .");
			return false;
		}
		this.genCode.genFun(symbol.getName(), genArgs(symbol.getName(), symbol
				.getValueType()), (short) lexi.getFuncArgs(symbol.getName())
				.size());
		word = lexi.readWord();
		if (!this.checkSubProgram(";")) {
			return false;
		}
		// lexi.addElem2Symbol(symbol);
		this.genCode.genFunRet(this.currentFuncName, 0, null, "void");
		word = lexi.readWord();
		this.currentFuncName = this.currentFuncNameStack
				.get(this.currentFuncNameStack.size() - 1);
		return true;
	}

	private boolean checkHead() {
		// program name ; !no arguments.
		word = lexi.readWord();
		if (word != null && word.equals("program")) {
			word = lexi.readWord();
			if (!lexi.isIdentity(word)) {
				System.out.println("Error:<" + lexi.line + "> " + word
						+ " does not seem a identity!");
				return false;
			}
			classname = word;
			if (!lexi.readWord().equals(";")) {
				System.out.println("Error:<" + lexi.line + "> "
						+ "must end of ';'");
				return false;
			}
			// generate java bytecode --- class header.
			genCode.genSkeleton(classname);
		} else {
			System.out.println("Error:<" + lexi.line
					+ "> pl0 struct: program + subprogram + .\n"
					+ "while program likes 'program pl0name ;'");
		}
		word = lexi.readWord();
		return true;
	}

	private boolean checkConst() {
		word = lexi.readWord();
		while (!word.equals(";")) {
			if (!lexi.isIdentity(word)) {
				System.out.println("Error:<" + lexi.line + "> "
						+ "const must follow by a identity.");
				return false;
			}
			if (lexi.isWordMarked(word)) {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " is already defined.");
				return false;
			}
			Symbol s1 = new Symbol();
			s1.setName(word);
			s1.setType(2);
			word = lexi.readWord();
			if (!word.equals("=")) {
				System.out.println("Error:<" + lexi.line + "> "
						+ "= is missing.");
				return false;
			}
			word = lexi.readWord();
			char c = lexi.getNextChar();
			if (c == '\'' && lexi.isChar(word)) {
				s1.setValueType("char");
				s1.setValue(word.charAt(0));
				genCode.genConstInt(s1.getName(), new Integer(word.charAt(0)));
			} else if (lexi.isBool(word)) {
				s1.setValueType("boolean");
				if (word.equals("true")) {
					s1.setValue(1);
					genCode.genConstInt(s1.getName(), new Integer(1));
				} else {
					s1.setValue(0);
					genCode.genConstInt(s1.getName(), new Integer(0));
				}
			} else if (lexi.isInt(word)) {
				s1.setValueType("integer");
				s1.setValue(new Integer(word));
				genCode.genConstInt(s1.getName(), new Integer(word));
			} else if (lexi.isFloat(word)) {
				s1.setValueType("real");
				s1.setValue(new Float(word));
				// float const must be write into Constant Pool
				genCode.genConstFloat(s1.getName(), new Float(word));
			} else {
				System.out.println("Error:<" + lexi.line + "> "
						+ "const must be char | integer | float | boolean");
				return false;
			}
			lexi.addElem2Symbol(s1);// add symbol to table
			word = lexi.readWord();
			if (word.equals(",")) {
				word = lexi.readWord();
			}
			if (word == null) {
				System.out.println("Error:<" + lexi.line + "> "
						+ "must end of ';'");
				return false;
			}
		}
		word = lexi.readWord();
		return true;
	}

	private boolean checkEnd(String end) {
		if (end.equals(".")) {
			word = lexi.readWord();
			if (word == null) {
				this.genCode.genFunRet("main", 0, null, "void");
			} else {
				System.out.println("Error<" + lexi.line + "> " + word
						+ " illegal here");
				return false;
			}
		}
		return true;
	}

}
