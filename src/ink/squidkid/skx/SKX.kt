package ink.squidkid.skx

import java.io.InputStream

/**
 * Sane Kotlin XML
 * type-safe xml builder/parser for kotlin
 * DSL style XML
 */

fun String.mul(times : Int) : String {
    var ret = ""
    for(x in 0..times-1)
        ret += this
    return ret
}

class XML {
    constructor(){}
    constructor(i : InputStream){
        this.root = parse(i).root
    }
    var root : Node = Node()
    fun addChild(c : Node) = root.addChild(c)
    fun removeChild(c : Node) = root.removeChild(c)
    fun plusAssign(c : Node) = addChild(c)
    fun minusAssign(c : Node) : Boolean = removeChild(c)
    override fun toString() : String {
        return root.render(StringBuilder(), "\t").toString()
    }
    fun node(init: Node.() -> Unit) : Node {
        val ret = Node().apply(init)
        root.addChild(ret)
        return ret
    }
}
class Node {
    var name : String  = "elem"
    var body : String = ""
    var attr : Array<Attr>  = arrayOf()
    var children : Array<Node> = arrayOf()
    constructor(name : String? = null, attr : Array<Attr>? = null, children  : Array<Node>? = null){
        if(name != null) this.name = name
        if(attr != null) this.attr = attr
        if(children != null) this.children = children
    }
    override fun equals(obj : Any?) : Boolean{
        if(obj is Node){
            var ret = this.name.equals(obj.name) && this.children.equals(obj.children) && this.attr.equals(obj.attr)
            return ret
        }
        return false
    }
    private fun indexOf(c : Node) : Int {
        var i = 0
        while(children.iterator().hasNext()){
            if(c.equals(children.iterator().next()))
                return i
            else
                i++
        }
        return -1
    }
    private fun indexOf(a : Attr) : Int {
        var i = 0
        val iter = attr.iterator()
        while(iter.hasNext()){
            if(a.equals(iter.next()))
                return i
            i++
        }
        return -1
    }

    fun hasChild(c : Node) : Boolean = this.indexOf(c) >= 0
    fun addChild(c : Node) {
        if(hasChild(c))
            return
        children = children.plus(c)
    }
    fun removeChild(c : Node) : Boolean {
        val sz = children.size
        children = children.filter { c.equals(it) }.toTypedArray()
        return sz != children.size
    }

    fun hasAttr(a : Attr) : Boolean = indexOf(a) >= 0
    fun addAttr(a : Attr) : Boolean {
        if(hasAttr(a)) return false
        this.attr = this.attr.plus(a)
        return true
    }
    fun removeAttr(a : Attr) : Boolean {
        val sz = this.attr.size
        this.attr = this.attr.filter { it.equals(a) }.toTypedArray()
        return sz != this.attr.size
    }

    fun plusAssign(c : Node) = addChild(c)
    fun plusAssign(a : Attr) : Boolean = addAttr(a)
    fun minusAssign(c : Node) : Boolean = removeChild(c)
    fun minusAssign(a : Attr) : Boolean = removeAttr(a)

    fun applyRecursively(f : Node.() -> Unit) {
        this.apply(f)
        children.forEach {
            it.applyRecursively(f)
        }
    }

    fun render(b : StringBuilder, idt : String = "", depth : Int = 0) : StringBuilder {
        b.append(idt.mul(depth)).append("<$name ")
        attr.forEach {
            b.append(it.name).append("=").append(it.value)
        }
        b.append(">\n").append(idt.mul(depth+1))
        children.forEach {
            it.render(b, idt, depth+1)
        }
        b.append(idt.mul(depth)) .append("</$name>\n")
        return b
    }

    fun node(init : Node.() -> Unit) : Node {
        val ret = Node().apply(init)
        this.addChild(ret)
        return ret
    }
    fun attr(init : Attr.() -> Unit) : Attr {
        val ret = Attr().apply(init)
        this.addAttr(ret)
        return ret
    }
}
class Attr(var name : String = "attrib", var value : String = "value")

fun xml( init : XML.() -> Unit): XML = XML().apply(init)
fun node(init : XML.() -> Unit): XML = XML().apply(init)
