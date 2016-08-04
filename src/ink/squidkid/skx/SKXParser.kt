
package ink.squidkid.skx

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * SKX Parser
 * builds on top of w3c parser
 */

fun NamedNodeMap.unroll(): Array<Attr> {
    var ret: Array<Attr> = arrayOf()
    for (i in 0..length - 1) {
        val n = item(i)
        ret = ret.plus(Attr(n.nodeName, n.nodeValue))
    }
    return ret
}

fun parse(i: InputStream): XML {
    val dFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dFactory.newDocumentBuilder()
    val doc = dBuilder.parse(i)
    return xml {
        name = doc.documentElement.tagName
        if (doc.documentElement.hasChildNodes()) {
            parse(this, doc.documentElement.childNodes)
        }
    }
}

private fun parse(parent: Node, nl: NodeList) {
    parent.apply {
        for (i in 0..nl.length - 1) {
            addChild(node new@ {
                val elem = nl.item(i)
                name = elem.nodeName
                attr = elem.attributes.unroll()
                if (elem.hasChildNodes()) {
                    parse(this@new, elem.childNodes)
                } else {
                    body = elem.nodeValue
                }
            })
        }
    }
}