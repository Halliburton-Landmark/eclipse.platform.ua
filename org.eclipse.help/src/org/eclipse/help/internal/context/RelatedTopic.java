/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.context;
import org.eclipse.help.IHelpResource;
import org.xml.sax.Attributes;
/**
 * Default implementation for a topic contribution
 */
public class RelatedTopic extends ContextsNode implements IHelpResource {
	protected String href;
	protected String label;
	public RelatedTopic(Attributes attrs) {
		super(attrs);
		if (attrs == null)
			return;
		href = attrs.getValue(ContextsNode.RELATED_HREF);
		this.label = attrs.getValue(ContextsNode.RELATED_LABEL);
		if (this.label == null)
			this.label = "undefined";
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * Returns the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @see ContextsNode#build(ContextsBuilder)
	 */
	public void build(ContextsBuilder builder) {
		builder.build(this);
	}
}