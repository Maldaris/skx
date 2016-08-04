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
    operator fun plusAssign(c : Node) = addChild(c)
    operator fun minusAssign(c : Node) : Unit = removeChild(c)
    operator fun plusAssign(x : XML) = addChild(x.root)
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
    var parent : Node?
    constructor(name : String? = null, attr : Array<Attr>? = null, children  : Array<Node>? = null, parent : Node? = null){
        if(name != null) this.name = name
        if(attr != null) this.attr = attr
        if(children != null) this.children = children
        this.parent = parent
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
        c.parent = this
    }
    fun removeChild(c : Node){
        children = children.filter { !c.equals(it) }.toTypedArray()
    }
    fun removeChild(s : String){
        children = children.filter { !it.name.equals(s) }.toTypedArray()
    }

    fun hasAttr(a : Attr) : Boolean = indexOf(a) >= 0
    fun addAttr(a : Attr) {
        if(hasAttr(a)) return
        this.attr = this.attr.plus(a)
    }
    fun removeAttr(a : Attr) {
        this.attr = this.attr.filter { !it.equals(a) }.toTypedArray()
    }

    fun findNode(s : String) : Node? {

        if(name.equals(s)) return this

        children.forEach {
            if (it.name.equals(s)) return it else {
                val n = it.findNode(s)
                if(n != null) return n
            }
        }

        return null
    }
    fun findNode(a : Attr) : Node? {

        if(hasAttr(a)) return this

        children.forEach {
            if(it.hasAttr(a)) return it
            else {
                val n = it.findNode(a)
                if(n != null) return n
            }
        }

        return null
    }

    fun signature(s : String) : Node?{
        val sp : Array<String> = s.split(".").toTypedArray()
        if(sp.size == 0)
            return null
        if(sp.size == 1)
            return findNode(s)
        return findNode(sp[0])?.signature(sp.slice(1..sp.size-1).joinToString(separator="."))
    }

    fun attr(s : String) : Attr? {
        attr.forEach {
            if(it.name.equals(s)) return it
        }
        return null
    }

    val first : Node?
        get() {
            if(children.size == 0) return null
            return children[0]
        }

    operator fun plusAssign(c : Node) = addChild(c)
    operator fun plusAssign(a : Attr) = addAttr(a)
    operator fun minusAssign(c : Node) = removeChild(c)
    operator fun minusAssign(a : Attr) = removeAttr(a)
    operator fun minusAssign(s : String) = removeChild(s)

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

fun xml(init : Node.() -> Unit): XML {
    val ret = XML()
    ret.root.apply(init)
    return ret
}
fun node(init : Node.() -> Unit): XML {
    val ret = XML()
    ret.root.apply(init)
    return ret
}