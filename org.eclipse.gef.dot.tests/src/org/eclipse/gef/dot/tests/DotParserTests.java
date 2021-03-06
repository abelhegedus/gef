/*******************************************************************************
 * Copyright (c) 2016 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tamas Miklossy (itemis AG) - initial implementation (bug #477980)
 *                                - Add support for polygon-based node shapes (bug #441352)
 *                                - modify grammar to allow empty attribute lists (bug #461506)
 *                                - Add support for all dot attributes (bug #461506)		
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.gef.dot.internal.DotFileUtils;
import org.eclipse.gef.dot.internal.language.DotInjectorProvider;
import org.eclipse.gef.dot.internal.language.dot.DotAst;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.eclipse.xtext.junit4.validation.ValidationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(XtextRunner.class)
@InjectWith(DotInjectorProvider.class)
public class DotParserTests {

	@Inject
	private ParseHelper<DotAst> parserHelper;

	@Inject
	private ValidationTestHelper validationTestHelper;

	private static List<String> dotTestHtmlLikeLabels;

	static {
		dotTestHtmlLikeLabels = new LinkedList<String>();
		Field[] declaredFields = DotTestHtmlLabels.class.getDeclaredFields();
		for (Field field : declaredFields) {
			try {
				String dotTestHtmlLikeLabel = (String) field.get(null);
				dotTestHtmlLikeLabels.add(dotTestHtmlLikeLabel);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testEmptyString() {
		try {
			DotAst dotAst = parserHelper.parse("");
			assertNull(dotAst);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testEmptyGraph() {
		testString(DotTestGraphs.EMPTY);
	}

	@Test
	public void testEmptyDirectedGraph() {
		testString(DotTestGraphs.EMPTY_DIRECTED);
	}

	@Test
	public void testEmptyStrictGraph() {
		testString(DotTestGraphs.EMPTY_STRICT);
	}

	@Test
	public void testEmptyStrictDirectedGraph() {
		testString(DotTestGraphs.EMPTY_STRICT_DIRECTED);
	}

	@Test
	public void testGraphWithOneNode() {
		testString(DotTestGraphs.ONE_NODE);
	}

	@Test
	public void testGraphWithOneNodeAndEmptyNodeAttributeList() {
		testString(DotTestGraphs.EMPTY_NODE_ATTRIBUTE_LIST);
	}

	@Test
	public void testGraphWithOneEdge() {
		testString(DotTestGraphs.ONE_EDGE);
	}

	@Test
	public void testDirectedGraphWithOneEdge() {
		testString(DotTestGraphs.ONE_DIRECTED_EDGE);
	}

	@Test
	public void testGraphWithOneEdgeAndEmptyEdgeAttributeList() {
		testString(DotTestGraphs.EMPTY_EDGE_ATTRIBUTE_LIST);
	}

	@Test
	public void testDirectedGraphWithOneEdgeAndEmptyEdgeAttributeList() {
		testString(DotTestGraphs.EMPTY_DIRECTED_EDGE_ATTRIBUTE_LIST);
	}

	@Test
	public void testGraphWithEmptyGraphAttributeStatement() {
		testString(DotTestGraphs.EMPTY_GRAPH_ATTRIBUTE_STATEMENT);
	}

	@Test
	public void testGraphWithEmptyNodeAttributeStatement() {
		testString(DotTestGraphs.EMPTY_NODE_ATTRIBUTE_STATEMENT);
	}

	@Test
	public void testGraphWithEmptyEdgeAttributeStatement() {
		testString(DotTestGraphs.EMPTY_EDGE_ATTRIBUTE_STATEMENT);
	}

	@Test
	public void testClusterLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs
					.CLUSTER_LABEL_HTML_LIKE(testDotHtmlLikeLabel).toString());
		}
	}

	@Test
	public void testEdgeHeadLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs
					.EDGE_HEADLABEL_HTML_LIKE(testDotHtmlLikeLabel).toString());
		}
	}

	@Test
	public void testEdgeLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs.EDGE_LABEL_HTML_LIKE(testDotHtmlLikeLabel)
					.toString());
		}
	}

	@Test
	public void testEdgeTailLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs
					.EDGE_TAILLABEL_HTML_LIKE(testDotHtmlLikeLabel).toString());
		}
	}

	@Test
	public void testEdgeXLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs.EDGE_XLABEL_HTML_LIKE(testDotHtmlLikeLabel)
					.toString());
		}
	}

	@Test
	public void testGraphLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs.GRAPH_LABEL_HTML_LIKE(testDotHtmlLikeLabel)
					.toString());
		}
	}

	@Test
	public void testNodeLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs.NODE_LABEL_HTML_LIKE(testDotHtmlLikeLabel)
					.toString());
		}
	}

	@Test
	public void testNodeXLabelHTMLLike() {
		for (String testDotHtmlLikeLabel : dotTestHtmlLikeLabels) {
			testString(DotTestGraphs.NODE_XLABEL_HTML_LIKE(testDotHtmlLikeLabel)
					.toString());
		}
	}

	@Test
	public void testNodeGroups() {
		testString(DotTestGraphs.NODE_GROUPS);
	}

	@Test
	public void testArrowShapesDeprecated() {
		testFile("arrowshapes_deprecated.dot");
	}

	@Test
	public void testArrowShapesDirectionBoth() {
		testFile("arrowshapes_direction_both.dot");
	}

	@Test
	public void testArrowShapesInvalidModifiers() {
		testFile("arrowshapes_invalid_modifiers.dot");
	}

	@Test
	public void testArrowShapesMultiple() {
		testFile("arrowshapes_multiple.dot");
	}

	@Test
	public void testArrowShapesSingle() {
		testFile("arrowshapes_single.dot");
	}

	@Test
	public void testLabeledGraph() {
		testFile("labeled_graph.dot");
	}

	@Test
	public void testNodeShapesPolygonBased() {
		testFile("nodeshapes_polygon_based.dot");
	}

	@Test
	public void testSimpleDigraph() {
		testFile("simple_digraph.dot");
	}

	@Test
	public void testSimpleGraph() {
		testFile("simple_graph.dot");
	}

	@Test
	public void testStyledGraph() {
		testFile("styled_graph.dot");
	}

	@Test
	public void testStyledGraph2() {
		testFile("styled_graph2.dot");
	}

	@Test
	public void testColoredGraph() {
		testFile("colored_graph.dot");
	}

	@Test
	public void testHtmlLikeLabels1() {
		testFile("html_like_labels1.dot");
	}

	@Test
	public void testHtmlLikeLabels2() {
		testFile("html_like_labels2.dot");
	}

	@Test
	public void testHtmlLikeLabels3() {
		testFile("html_like_labels3.dot");
	}

	@Test
	public void testHtmlLikeLabels4() {
		testFile("html_like_labels4.dot");
	}

	private void testFile(String fileName) {
		String fileContents = DotFileUtils
				.read(new File(DotTestUtils.RESOURCES_TESTS + fileName));
		testString(fileContents);
	}

	private void testString(String text) {
		try {
			DotAst dotAst = parserHelper.parse(text);
			assertNotNull(dotAst);
			validationTestHelper.assertNoErrors(dotAst);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
