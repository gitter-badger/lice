/**
 * Created by ice1000 on 2017/2/18.
 *
 * @author ice1000
 */
package lice.compiler.parse

//val Char.isDigit: Boolean
//	get() = this >= '0' && this <= '9'

fun String.isInt() =
		fold(true, { res, char ->
			res && char.isDigit()
		})

fun Char.safeLower(): Char {
	if (this >= 'A' && this <= 'Z') return this - ('A' - 'a');
	return this
}

fun String.isHexInt(): Boolean {
	if (length <= 2) return false
	return (2..length - 1)
			.map { this[it] }
			.none { it < 'a' || it > 'f' }
}

//fun isHex(string: String) =

