/**
 * Created by ice1000 on 2017/2/26.
 *
 * @author ice1000
 */
@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Standard")
@file:JvmMultifileClass

package lice.core

import lice.compiler.model.EmptyNode
import lice.compiler.model.Value.Objects.Nullptr
import lice.compiler.model.ValueNode
import lice.compiler.util.InterpretException.Factory.tooFewArgument
import lice.compiler.util.InterpretException.Factory.typeMisMatch
import lice.compiler.util.SymbolList

class Pair<out A, out B>(
		val first: A,
		val second: B) {
	override fun toString(): String {
		return "[$first $second]"
	}
}

inline fun SymbolList.addListFunctions() {
	addFunction("[|]", { ls ->
		ValueNode(ls.foldRight(null) { value, pairs: Any? ->
			Pair(value.eval().o, pairs)
		}, Pair::class.java)
	})
	addFunction("head", { ls ->
		val a = ls[0].eval()
		if (a.o is Pair<*, *>) ValueNode(a.o.first, Any::class.java)
		else typeMisMatch("Pair", a)
	})
	addFunction("tail", { ls ->
		val a = ls[0].eval()
		if (a.o is Pair<*, *>) when (a.o.second) {
			null -> EmptyNode
			else -> ValueNode(a.o.second)
		}
		else typeMisMatch("Pair", a)
	})
}

inline fun SymbolList.addCollectionsFunctions() {
	addFunction("[]", { ls ->
		ValueNode(ls.map { it.eval().o })
	})
	addFunction("..", { ls ->
		if (ls.size < 2)
			tooFewArgument(2, ls.size)
		val begin = ls[0].eval().o as Int
		val end = ls[1].eval().o as Int
		val progression = when {
			begin <= end -> begin..end
			else -> (begin..end).reversed()
		}
		ValueNode(progression.toList())
	})
	addFunction("for-each", { ls ->
		if (ls.size < 3)
			tooFewArgument(3, ls.size)
		val i = ls[0].eval()
		if (i.o !is String) typeMisMatch("String", i)
		val a = ls[1].eval()
		when (a.o) {
			is Collection<*> -> {
				var ret: Any? = null
				a.o.forEach {
					setVariable(i.o, ValueNode(it ?: Nullptr))
					ret = ls[2].eval().o
				}
				ValueNode(ret ?: Nullptr)
			}
			else -> typeMisMatch("List", a)
		}
	})
	addFunction("size", { ls ->
		val i = ls[0].eval()
		when (i.o) {
			is Collection<*> -> ValueNode(i.o.size)
			else -> ValueNode(ls.size)
		}
	})
	addFunction("reverse", { ls ->
		val i = ls[0].eval()
		when (i.o) {
			is Collection<*> -> ValueNode(i.o.reversed())
			else -> ValueNode(ls.size)
		}
	})
	addFunction("count", { ls ->
		val i = ls[0].eval()
		val e = ls[1].eval()
		when (i.o) {
			is Collection<*> -> ValueNode(i.o.count { e.o == it })
			else -> ValueNode(0)
		}
	})
	addFunction("empty?", { ls ->
		ValueNode((ls[0].eval().o as? Collection<*>)?.isEmpty() ?: true)
	})
	addFunction("in?", { ls ->
		val i = ls[0].eval()
		val e = ls[1].eval()
		when (i.o) {
			is Collection<*> -> ValueNode(e.o in i.o)
			else -> ValueNode(false)
		}
	})
}