package com.intrbiz.balsa.apt;

import java.util.Stack;

import org.apache.maven.doxia.logging.Log;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;

import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.component.TextNode;
import com.intrbiz.balsa.view.core.fragment.FragmentComponent;
import com.intrbiz.balsa.view.core.fragment.FragmentRenderer;
import com.intrbiz.balsa.view.core.generic.GenericComponent;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.express.operator.StringLiteral;
import com.intrbiz.express.value.ValueExpression;

public class BalsaSink2 implements Sink
{
    private boolean inTitle = false;

    private String title = null;

    protected boolean verbatim = false;

    protected boolean vertbatimBoxed = false;

    private Component root;

    private Stack<Component> components = new Stack<Component>();

    public BalsaSink2()
    {
        super();
    }

    public BalsaSink2(String title)
    {
        this();
        this.title = title;
    }

    public Component getRoot()
    {
        return this.root;
    }

    public String getTitle()
    {
        return this.title;
    }

    protected void push(Component comp)
    {
        // System.out.println("Push [" + comp.getName() + "]");
        this.components.push(comp);
    }

    protected Component pop(String expectedName)
    {
        // System.out.println("Pop [" + expectedName + "]");
        Component comp = this.components.pop();
        if (!expectedName.equals(comp.getName())) throw new RuntimeException("Element mismatch, expecting: " + expectedName + " got: " + comp.getName());
        if (this.components.isEmpty()) throw new RuntimeException("Stack is empty, popped too much!");
        // link
        Component parent = this.components.peek();
        parent.addChild(comp);
        return comp;
    }

    protected Component peek()
    {
        return this.components.peek();
    }

    protected GenericComponent genericComponent(String name)
    {
        GenericComponent component = new GenericComponent();
        component.setName(name);
        component.setRenderer(new GenericRenderer());
        return component;
    }

    protected Component addAttribute(Component component, String name, String value)
    {
        component.addAttribute(name, new ValueExpression(new StringLiteral(value, false)));
        return component;
    }

    protected BalsaSink2 write(String w)
    {
        TextNode tn = new TextNode();
        tn.setText(w);
        peek().addChild(tn);

        return this;
    }

    protected void writeVerbatim(String w)
    {
        this.write(w);
    }

    @Override
    public void enableLogging(Log arg0)
    {
    }

    @Override
    public void head()
    {
    }

    @Override
    public void head(SinkEventAttributes attributes)
    {
    }

    @Override
    public void head_()
    {
    }

    @Override
    public void title()
    {
        this.inTitle = true;
    }

    @Override
    public void title(SinkEventAttributes attributes)
    {
        this.inTitle = true;
    }

    @Override
    public void title_()
    {
        this.inTitle = false;
    }

    @Override
    public void author()
    {
    }

    @Override
    public void author(SinkEventAttributes attributes)
    {
    }

    @Override
    public void author_()
    {
    }

    @Override
    public void date()
    {
    }

    @Override
    public void date(SinkEventAttributes attributes)
    {
    }

    @Override
    public void date_()
    {
    }

    @Override
    public void body()
    {
        this.body(null);
    }

    @Override
    public void body(SinkEventAttributes attributes)
    {
        this.root = new FragmentComponent();
        this.root.setName("fragment");
        this.root.setRenderer(new FragmentRenderer());
        this.components.push(root);
        // System.out.println("Pushed root");
    }

    @Override
    public void body_()
    {
        this.components.pop();
        // System.out.println("Popped root");
    }

    @Override
    public void sectionTitle()
    {
        this.sectionTitle(1, null);
    }

    @Override
    public void sectionTitle_()
    {
        this.sectionTitle_(1);
    }

    @Override
    public void section1()
    {
        this.section(1, null);
    }

    @Override
    public void section1_()
    {
        this.section_(1);
    }

    @Override
    public void sectionTitle1()
    {
        this.sectionTitle(1, null);
    }

    @Override
    public void sectionTitle1_()
    {
        this.sectionTitle_(1);
    }

    @Override
    public void section2()
    {
        this.section(2, null);
    }

    @Override
    public void section2_()
    {
        this.section_(2);
    }

    @Override
    public void sectionTitle2()
    {
        this.sectionTitle(2, null);
    }

    @Override
    public void sectionTitle2_()
    {
        this.sectionTitle_(2);
    }

    @Override
    public void section3()
    {
        this.section(3, null);
    }

    @Override
    public void section3_()
    {
        this.section_(3);
    }

    @Override
    public void sectionTitle3()
    {
        this.sectionTitle(3, null);
    }

    @Override
    public void sectionTitle3_()
    {
        this.sectionTitle_(3);
    }

    @Override
    public void section4()
    {
        this.section(4, null);
    }

    @Override
    public void section4_()
    {
        this.section_(4);
    }

    @Override
    public void sectionTitle4()
    {
        this.sectionTitle(4, null);
    }

    @Override
    public void sectionTitle4_()
    {
        this.sectionTitle_(4);
    }

    @Override
    public void section5()
    {
        this.section(5, null);
    }

    @Override
    public void section5_()
    {
        this.section_(5);
    }

    @Override
    public void sectionTitle5()
    {
        this.sectionTitle(5, null);
    }

    @Override
    public void sectionTitle5_()
    {
        this.sectionTitle_(5);
    }

    @Override
    public void section(int level, SinkEventAttributes attributes)
    {
        Component div = genericComponent("div");
        addAttribute(div, "class", "section" + level);
        push(div);
    }

    @Override
    public void section_(int level)
    {
        pop("div");
    }

    @Override
    public void sectionTitle(int level, SinkEventAttributes attributes)
    {
        Component h = genericComponent("h" + level);
        push(h);
    }

    @Override
    public void sectionTitle_(int level)
    {
        pop("h" + level);
    }

    @Override
    public void list()
    {
        this.list(null);
    }

    @Override
    public void list(SinkEventAttributes attributes)
    {
        Component ul = genericComponent("ul");
        push(ul);
    }

    @Override
    public void list_()
    {
        pop("ul");
    }

    @Override
    public void listItem()
    {
        this.listItem(null);
    }

    @Override
    public void listItem(SinkEventAttributes attributes)
    {
        Component li = genericComponent("li");
        push(li);
    }

    @Override
    public void listItem_()
    {
        pop("li");
    }

    @Override
    public void numberedList(int numbering)
    {
        this.numberedList(numbering, null);
    }

    @Override
    public void numberedList(int numbering, SinkEventAttributes attributes)
    {
        Component ol = genericComponent("ol");
        push(ol);
    }

    @Override
    public void numberedList_()
    {
        pop("ol");
    }

    @Override
    public void numberedListItem()
    {
        this.numberedListItem(null);
    }

    @Override
    public void numberedListItem(SinkEventAttributes attributes)
    {
        this.listItem(attributes);
    }

    @Override
    public void numberedListItem_()
    {
        this.listItem_();
    }

    @Override
    public void definitionList()
    {
        this.definitionList(null);
    }

    @Override
    public void definitionList(SinkEventAttributes attributes)
    {
        Component dl = genericComponent("dl");
        push(dl);
    }

    @Override
    public void definitionList_()
    {
        pop("dl");
    }

    @Override
    public void definitionListItem()
    {
    }

    @Override
    public void definitionListItem(SinkEventAttributes attributes)
    {
    }

    @Override
    public void definitionListItem_()
    {
    }

    @Override
    public void definition()
    {
        this.definition(null);
    }

    @Override
    public void definition(SinkEventAttributes attributes)
    {
        Component dd = genericComponent("dd");
        push(dd);
    }

    @Override
    public void definition_()
    {
        pop("dd");
    }

    @Override
    public void definedTerm()
    {
        this.definedTerm(null);
    }

    @Override
    public void definedTerm(SinkEventAttributes attributes)
    {
        Component dt = genericComponent("dt");
        push(dt);
    }

    @Override
    public void definedTerm_()
    {
        pop("dt");
    }

    @Override
    public void figure()
    {
        this.figure(null);
    }

    @Override
    public void figure(SinkEventAttributes attributes)
    {
        Component div = genericComponent("div");
        addAttribute(div, "class", "figure");
        push(div);
    }

    @Override
    public void figure_()
    {
        pop("div");
    }

    @Override
    public void figureCaption()
    {
        this.figureCaption(null);
    }

    @Override
    public void figureCaption(SinkEventAttributes attributes)
    {
        Component span = genericComponent("span");
        addAttribute(span, "class", "figure-caption");
        push(span);
    }

    @Override
    public void figureCaption_()
    {
        pop("span");
    }

    @Override
    public void figureGraphics(String name)
    {
        this.figureGraphics(name, null);
    }

    @Override
    public void figureGraphics(String src, SinkEventAttributes attributes)
    {
        Component img = genericComponent("img");
        addAttribute(img, "src", "src");
        push(img);
        pop("img");
    }

    @Override
    public void table()
    {
        this.table(null);
    }

    @Override
    public void table(SinkEventAttributes attributes)
    {
        Component table = genericComponent("table");
        push(table);
    }

    @Override
    public void table_()
    {
        pop("table");
    }

    @Override
    public void tableRows(int[] justification, boolean grid)
    {
    }

    @Override
    public void tableRows_()
    {
    }

    @Override
    public void tableRow()
    {
        this.tableRow(null);
    }

    @Override
    public void tableRow(SinkEventAttributes attributes)
    {
        Component tr = genericComponent("tr");
        push(tr);
    }

    @Override
    public void tableRow_()
    {
        pop("tr");
    }

    @Override
    public void tableCell()
    {
        this.tableCell((SinkEventAttributes) null);
    }

    @Override
    public void tableCell(String width)
    {
        this.tableCell((SinkEventAttributes) null);
    }

    @Override
    public void tableCell(SinkEventAttributes attributes)
    {
        Component td = genericComponent("td");
        push(td);
    }

    @Override
    public void tableCell_()
    {
        pop("td");
    }

    @Override
    public void tableHeaderCell()
    {
        this.tableHeaderCell((SinkEventAttributes) null);
    }

    @Override
    public void tableHeaderCell(String width)
    {
        this.tableHeaderCell((SinkEventAttributes) null);
    }

    @Override
    public void tableHeaderCell(SinkEventAttributes attributes)
    {
        Component th = genericComponent("th");
        push(th);
    }

    @Override
    public void tableHeaderCell_()
    {
        pop("th");
    }

    @Override
    public void tableCaption()
    {
        this.tableCaption(null);
    }

    @Override
    public void tableCaption(SinkEventAttributes attributes)
    {
        Component span = genericComponent("span");
        addAttribute(span, "class", "table-caption");
        push(span);
    }

    @Override
    public void tableCaption_()
    {
        pop("span");
    }

    @Override
    public void paragraph()
    {
        this.paragraph(null);
    }

    @Override
    public void paragraph(SinkEventAttributes attributes)
    {
        Component p = genericComponent("p");
        push(p);
    }

    @Override
    public void paragraph_()
    {
        pop("p");
    }

    @Override
    public void verbatim(boolean boxed)
    {
        this.verbatim = true;
        this.vertbatimBoxed = boxed;
    }

    @Override
    public void verbatim(SinkEventAttributes attributes)
    {
        this.verbatim = true;
        this.vertbatimBoxed = "boxed".equalsIgnoreCase(attributes == null ? null : (String) attributes.getAttribute("decoration"));
        Component pre = genericComponent("pre");
        if (this.vertbatimBoxed) addAttribute(pre, "class", "boxed");
        push(pre);
    }

    @Override
    public void verbatim_()
    {
        pop("pre");
        this.verbatim = false;
    }

    @Override
    public void horizontalRule()
    {
        this.horizontalRule(null);
    }

    @Override
    public void horizontalRule(SinkEventAttributes attributes)
    {
        Component hr = genericComponent("hr");
        push(hr);
        pop("hr");
    }

    @Override
    public void pageBreak()
    {
    }

    @Override
    public void anchor(String name)
    {
        this.anchor(name, null);
    }

    @Override
    public void anchor(String name, SinkEventAttributes attributes)
    {
        Component a = genericComponent("a");
        addAttribute(a, "name", name);
        push(a);
    }

    @Override
    public void anchor_()
    {
        pop("a");
    }

    @Override
    public void link(String name)
    {
        this.link(name, null);
    }

    @Override
    public void link(String name, SinkEventAttributes attributes)
    {
        Component a = genericComponent("a");
        addAttribute(a, "href", name);
        push(a);
    }

    @Override
    public void link_()
    {
        pop("a");
    }

    @Override
    public void italic()
    {
        Component span = genericComponent("span");
        addAttribute(span, "class", "italic");
        push(span);
    }

    @Override
    public void italic_()
    {
        pop("span");
    }

    @Override
    public void bold()
    {
        Component strong = genericComponent("strong");
        push(strong);
    }

    @Override
    public void bold_()
    {
        pop("strong");
    }

    @Override
    public void monospaced()
    {
        Component span = genericComponent("span");
        addAttribute(span, "class", "monospaced");
        push(span);
    }

    @Override
    public void monospaced_()
    {
        pop("span");
    }

    @Override
    public void lineBreak()
    {
        this.lineBreak(null);
    }

    @Override
    public void lineBreak(SinkEventAttributes attributes)
    {
        Component br = genericComponent("br");
        push(br);
        pop("br");
    }

    @Override
    public void nonBreakingSpace()
    {
        this.write("&nbsp;");
    }

    @Override
    public void text(String text)
    {
        this.text(text, null);
    }

    @Override
    public void text(String text, SinkEventAttributes attributes)
    {
        if (this.inTitle)
        {
            this.title = text;
        }
        else
        {
            if (this.verbatim)
            {
                this.writeVerbatim(text);
            }
            else
            {
                this.write(text);
            }
        }
    }

    @Override
    public void rawText(String text)
    {
    }

    @Override
    public void comment(String comment)
    {
        // IGNORE
    }

    @Override
    public void unknown(String name, Object[] requiredParams, SinkEventAttributes attributes)
    {
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close()
    {
    }

}
