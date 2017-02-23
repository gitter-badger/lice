package lice.compiler.util

import lice.compiler.model.Ast
import lice.compiler.model.Value
import lice.compiler.model.Value.Objects.nullptr
import lice.compiler.parse.buildNode
import lice.compiler.parse.createAst
import lice.compiler.parse.mapAst
import java.io.File

/**
 * Created by ice1000 on 2017/2/17.
 *
 * @author ice1000
 */
class SymbolList(init: Boolean = true) {
	val functionMap: MutableMap<String, Int>
	val functionList: MutableList<(List<Value>) -> Value>

	val variableMap: MutableMap<String, Int>
	val variableList: MutableList<Value>

	val typeMap: MutableMap<String, Int>
	val typeList: MutableList<Class<*>>

	init {
		functionMap = mutableMapOf()
		functionList = mutableListOf()
		variableMap = mutableMapOf()
		variableList = mutableListOf()
		typeMap = mutableMapOf()
		typeList = mutableListOf()
		if (init) initialize()
	}

	fun initialize() {
		addFunction("+", { ls ->
//			println("+ called!")
//			ls.forEach { verboseOutput() }
			Value(ls.fold(0) { sum, value ->
				if (value.o is Int) value.o + sum
				else InterpretException.typeMisMatch("Int", value)
			})
		})
		addFunction("-", { ls ->
			Value(ls.fold(ls[0].o as Int shl 1) { delta, value ->
				if (value.o is Int) delta - value.o
				else InterpretException.typeMisMatch("Int", value)
			})
		})
		addFunction("/", { ls ->
			Value(ls.fold((ls[0].o as Int).squared()) { res, value ->
				if (value.o is Int) res / value.o
				else InterpretException.typeMisMatch("Int", value)
			})
		})
		addFunction("*", { ls ->
			Value(ls.fold(1) { sum, value ->
				if (value.o is Int) value.o * sum
				else InterpretException.typeMisMatch("Int", value)
			})
		})
		addFunction("[]", { ls -> Value(ls.map { it.o }) })
		addFunction("", { ls ->
//			ls.size.verboseOutput()
			ls.forEach { println("${it.o.toString()} => ${it.type.name}") }
			ls[ls.size - 1]
		})
		addFunction("new", { ls ->
			var obj: Any? = null
			loop@for (constructor in Class
					.forName(ls[0].o as String)
					.constructors) {
				obj = constructor.newInstance(*ls
						.subList(1, ls.size)
//						.apply { forEach { it.o.println() } }
						.toTypedArray()
				)
				if (obj != null) break@loop
			}
			Value(obj ?: showError("constructor not found!"))
		})
		addFunction("str-con", { ls ->
			Value(ls.fold(StringBuilder(ls.size)) { sb, value ->
				if (value.o is String) sb.append(value.o)
				else InterpretException.typeMisMatch("String", value)
			}.toString())
		})
		addFunction("print", { ls ->
			ls.forEach { println(it.o) }
			ls[ls.size - 1]
		})
		addFunction("type", { ls ->
			ls.forEach { println(it.type.canonicalName) }
			ls[ls.size - 1]
		})
		addFunction("gc", {
			System.gc()
			nullptr
		})
		addFunction("eval", { ls ->
			val o = ls[0].o
			if (o is String) {
				val symbolList = SymbolList(true)
				val stringTreeRoot = buildNode(o)
				Value(mapAst(stringTreeRoot, symbolList).eval())
			} else InterpretException.typeMisMatch("String", ls[0])
		})
		addType("Int", Int::class.java)
		addType("Double", Double::class.java)
		addType("String", String::class.java)
	}

	fun addFunction(name: String, node: (List<Value>) -> Value): Int {
		functionMap.put(name, functionList.size)
		functionList.add(node)
		return functionList.size - 1
	}

	fun addVariable(name: String, value: Value) {
		variableMap.put(name, variableList.size)
		variableList.add(value)
	}

	fun addType(name: String, clazz: Class<*>) {
		typeMap.put(name, typeList.size)
		typeList.add(clazz)
	}

	fun getVariableId(name: String) = variableMap[name]

	fun getTypeId(name: String) = typeMap[name]

	fun getFunctionId(name: String) = functionMap[name]

	fun getVariable(id: Int) = variableList[id]

	fun setVariable(id: Int, newValue: Value) {
		variableList[id] = newValue
	}

	fun getFunction(id: Int) = functionList[id]
}
