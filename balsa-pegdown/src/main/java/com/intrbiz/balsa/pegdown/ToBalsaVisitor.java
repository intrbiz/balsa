package com.intrbiz.balsa.pegdown;

import java.io.StringReader;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrongEmphSuperNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;

import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.util.Util;
import com.intrbiz.balsa.view.component.Component;
import com.intrbiz.balsa.view.component.View;
import com.intrbiz.balsa.view.core.fragment.FragmentComponent;
import com.intrbiz.balsa.view.core.fragment.FragmentRenderer;
import com.intrbiz.balsa.view.core.generic.GenericComponent;
import com.intrbiz.balsa.view.core.generic.GenericRenderer;
import com.intrbiz.balsa.view.core.html.CodeComponent;
import com.intrbiz.balsa.view.core.html.CodeRenderer;
import com.intrbiz.balsa.view.core.html.PreComponent;
import com.intrbiz.balsa.view.core.html.PreRenderer;
import com.intrbiz.balsa.view.parser.Parser;
import com.intrbiz.express.operator.BooleanLiteral;
import com.intrbiz.express.operator.StringLiteral;
import com.intrbiz.express.value.ValueExpression;

public class ToBalsaVisitor implements Visitor
{
    private final Logger logger = Logger.getLogger(ToBalsaVisitor.class);

    //
    
    private boolean hideTitle;

    private View view;

    private Component root;

    private Stack<Component> components = new Stack<Component>();

    private Component title;

    // table state

    protected TableNode currentTableNode;

    protected int currentTableColumn;

    protected boolean inTableHeader;

    // section tracking

    protected Stack<Integer> sections = new Stack<Integer>();

    public ToBalsaVisitor(View view, boolean hideTitle)
    {
        super();
        this.view = view;
        this.hideTitle = hideTitle;
    }
    
    public ToBalsaVisitor(View view)
    {
        this(view, true);
    }

    public void startDocument()
    {
        this.root = new FragmentComponent();
        this.root.setView(this.view);
        this.root.setName("fragment");
        this.root.setRenderer(new FragmentRenderer());
        this.components.push(root);
    }

    public void endDocument()
    {
        while (!this.sections.isEmpty())
        {
            this.pop("div");
            this.sections.pop();
        }
        // title ?
        if (this.view.getMetadata().containsAttribute("title"))
        {
            this.addAttribute(this.root, "title", (String) this.view.getMetadata().getAttribute("title"));
        }
        else if (this.title != null)
        {
            String titleText = this.getText(this.title);
            this.addAttribute(this.root, "title", titleText);
            this.view.getMetadata().setAttribute("title", titleText);
            // suppress the title from being rendered
            if (this.hideTitle)
            {
                this.title.addAttribute("rendered", new ValueExpression(new BooleanLiteral(false)));
            }
        }
    }

    public Component getRoot()
    {
        return this.root;
    }

    protected void push(Component comp)
    {
        this.components.push(comp);
    }

    protected Component pop(String expectedName)
    {
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

    protected PreComponent preComponent()
    {
        PreComponent component = new PreComponent();
        component.setView(this.view);
        component.setName("pre");
        component.setRenderer(new PreRenderer());
        return component;
    }
    
    protected CodeComponent codeComponent()
    {
        CodeComponent component = new CodeComponent();
        component.setView(this.view);
        component.setName("code");
        component.setRenderer(new CodeRenderer());
        return component;
    }

    protected GenericComponent genericComponent(String name)
    {
        GenericComponent component = new GenericComponent();
        component.setView(this.view);
        component.setName(name);
        component.setRenderer(new GenericRenderer());
        return component;
    }

    protected Component addAttribute(Component component, String name, String value)
    {
        component.addAttribute(name, new ValueExpression(new StringLiteral(value, false)));
        return component;
    }

    protected ToBalsaVisitor write(String w)
    {
        peek().addText(w);
        return this;
    }

    //

    @SuppressWarnings("unused")
    public void visit(RootNode node)
    {
        for (ReferenceNode refNode : node.getReferences())
        {
            // TODO
        }
        for (AbbreviationNode abbrNode : node.getAbbreviations())
        {
            // TODO
        }
        visitChildren(node);
    }

    public void visit(AbbreviationNode node)
    {
    }

    public void visit(AutoLinkNode node)
    {
        pushLink(node.getText(), null, node.getText());
    }

    public void visit(BlockQuoteNode node)
    {
        pushTag(node, "blockquote");
    }

    public void visit(BulletListNode node)
    {
        pushTag(node, "ul");
    }

    public void visit(CodeNode node)
    {
        pushTag(node, "code");
    }

    public void visit(DefinitionListNode node)
    {
        pushTag(node, "dl");
    }

    public void visit(DefinitionNode node)
    {
        pushTag(node, "dd");
    }

    public void visit(DefinitionTermNode node)
    {
        pushTag(node, "dt");
    }

    public void visit(ExpImageNode node)
    {
        pushImageTag(node, node.url);
    }

    public void visit(ExpLinkNode node)
    {
        pushLink(node.url, node.title, node.getChildren());
    }

    public void visit(HeaderNode node)
    {
        if ((!this.sections.isEmpty()) && this.sections.peek() == node.getLevel())
        {
            this.sections.pop();
            this.pop("div");
        }
        //
        this.push(this.addAttribute(this.genericComponent("div"), "class", "section-" + node.getLevel()));
        this.sections.push(node.getLevel());
        //
        Component header = this.genericComponent("h" + node.getLevel());
        if (this.title == null) this.title = header;
        this.push(header);
        this.visitChildren(node);
        this.pop("h" + node.getLevel());
    }

    public void visit(HtmlBlockNode node)
    {
        this.parseHtml(node.getText());
    }

    public void visit(InlineHtmlNode node)
    {
        // currently we can't support inline HTML
        this.write(node.getText());
    }

    protected void parseHtml(String html)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE balsa SYSTEM \"http://balsa.intrbiz.net/balsa.dtd\">");
            sb.append("<?RenderLibrary com.intrbiz.balsa?>");
            sb.append("<fragment xmlns=\"com.intrbiz.balsa\">");
            sb.append(html);
            sb.append("</fragment>");
            //
            Component comp = Parser.parse(BalsaContext.Balsa(), this.view, new StringReader(sb.toString()));
            this.components.peek().addChild(comp);
        }
        catch (Exception e)
        {
            logger.warn("Failed to parse inline HTML block:\r\n" + html, e);
        }
    }

    public void visit(ListItemNode node)
    {
        pushTag(node, "li");
    }

    public void visit(MailLinkNode node)
    {
        pushLink("mailto:" + node.getText(), null, node.getText());
    }

    public void visit(OrderedListNode node)
    {
        pushTag(node, "ol");
    }

    public void visit(ParaNode node)
    {
        pushTag(node, "p");
    }

    public void visit(QuotedNode node)
    {
        switch (node.getType())
        {
            case DoubleAngle:
                this.write("&laquo;");
                visitChildren(node);
                this.write("&raquo;");
                break;
            case Double:
                this.write("&ldquo;");
                visitChildren(node);
                this.write("&rdquo;");
                break;
            case Single:
                this.write("&lsquo;");
                visitChildren(node);
                this.write("&rsquo;");
                break;
        }
    }

    public void visit(ReferenceNode node)
    {
        // reference nodes are not printed
    }

    public void visit(RefImageNode node)
    {
        // TODO
    }

    public void visit(RefLinkNode node)
    {
        // TODO
    }

    public void visit(SimpleNode node)
    {
        switch (node.getType())
        {
            case Apostrophe:
                this.write("&rsquo;");
                break;
            case Ellipsis:
                this.write("&hellip;");
                break;
            case Emdash:
                this.write("&mdash;");
                break;
            case Endash:
                this.write("&ndash;");
                break;
            case HRule:
                this.push(this.genericComponent("hr"));
                this.pop("hr");
                break;
            case Linebreak:
                this.push(this.genericComponent("br"));
                this.pop("br");
                break;
            case Nbsp:
                this.write("&nbsp;");
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void visit(StrongEmphSuperNode node)
    {
        if (node.isClosed())
        {
            if (node.isStrong())
                pushTag(node, "strong");
            else
                pushTag(node, "em");
        }
        else
        {
            // sequence was not closed, treat open chars as ordinary chars
            this.write(node.getChars());
            visitChildren(node);
        }
    }

    public void visit(TableBodyNode node)
    {
        pushTag(node, "tbody");
    }

    @Override
    public void visit(TableCaptionNode node)
    {
        this.pushTag(node, "caption");
    }

    public void visit(TableCellNode node)
    {
        String tag = inTableHeader ? "th" : "td";
        // List<TableColumnNode> columns = currentTableNode.getColumns();
        // TableColumnNode column = columns.get(Math.min(currentTableColumn, columns.size() - 1));
        this.pushTag(node, tag);
        currentTableColumn += node.getColSpan();
    }

    public void visit(TableHeaderNode node)
    {
        inTableHeader = true;
        pushTag(node, "thead");
        inTableHeader = false;
    }

    public void visit(TableNode node)
    {
        currentTableNode = node;
        pushTag(node, "table");
        currentTableNode = null;
    }

    public void visit(TableRowNode node)
    {
        currentTableColumn = 0;
        pushTag(node, "tr");
    }

    @Override
    public void visit(TableColumnNode node)
    {
    }

    public void visit(VerbatimNode node)
    {
        String codeType = this.view.getMetadata().getAttribute("code");
        if (Util.isEmpty(codeType))
        {
            PreComponent pre = this.preComponent();
            this.addAttribute(pre, "class", "plain");
            pre.setText("\r\n" + node.getText());
            this.push(pre);
            this.pop("pre");
        }
        else
        {
            PreComponent pre = this.preComponent();
            this.push(pre);
            CodeComponent code = this.codeComponent();
            this.addAttribute(code, "class", codeType);
            code.setText(node.getText());
            this.push(code);
            this.pop("code");
            this.pop("pre");
        }
    }

    public void visit(WikiLinkNode node)
    {
        String url = node.getText().replace(' ', '-');
        pushLink(url, null, node.getText());
    }

    public void visit(TextNode node)
    {
        this.write(node.getText());
    }

    public void visit(SpecialTextNode node)
    {
        this.write(node.getText());
    }

    public void visit(SuperNode node)
    {
        visitChildren(node);
    }

    public void visit(Node node)
    {
        throw new RuntimeException("Don't know how to handle node " + node);
    }

    //

    protected void visitChildren(SuperNode node)
    {
        for (Node child : node.getChildren())
        {
            child.accept(this);
        }
    }

    protected void pushTag(TextNode node, String tag)
    {
        this.push(this.genericComponent(tag));
        this.write(node.getText());
        this.pop(tag);
    }

    protected void pushTag(SuperNode node, String tag)
    {
        this.push(this.genericComponent(tag));
        visitChildren(node);
        this.pop(tag);
    }

    protected void pushImageTag(SuperNode imageNode, String url)
    {
        this.push(this.addAttribute(this.genericComponent("img"), "src", url));
        this.pop("img");
    }
    
    protected void pushLink(String url, String title, String text)
    {
        GenericComponent a = this.genericComponent("a");
        this.addAttribute(a, "href", url);
        if (! Util.isEmpty(title)) this.addAttribute(a, "title", title);
        this.push(a);
        this.write(text);
        this.pop("a");
    }
    
    protected void pushLink(String url, String title, List<Node> text)
    {
        GenericComponent a = this.genericComponent("a");
        this.addAttribute(a, "href", url);
        if (! Util.isEmpty(title)) this.addAttribute(a, "title", title);
        this.push(a);
        for (Node node : text)
        {
            node.accept(this);
        }
        this.pop("a");
    }

    protected String getText(Component comp)
    {
        StringBuilder sb = new StringBuilder();
        if (comp.getText() != null)
        {
            sb.append(comp.getText().get(null, null));
        }
        for (Component child : comp.getChildren())
        {
            if (child.getText() != null)
            {
                sb.append(child.getText().get(null, null));
            }
            if (child instanceof com.intrbiz.balsa.view.component.TextNode)
            {
                com.intrbiz.balsa.view.component.TextNode node = (com.intrbiz.balsa.view.component.TextNode) child;
                sb.append(node.getText().get(null, null));
            }
        }
        return sb.toString();
    }
}
