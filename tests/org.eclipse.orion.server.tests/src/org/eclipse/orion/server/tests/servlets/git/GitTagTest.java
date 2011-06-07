/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.tests.servlets.git;

import static org.junit.Assert.assertEquals;

import java.net.HttpURLConnection;
import java.net.URI;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.orion.internal.server.servlets.ProtocolConstants;
import org.eclipse.orion.server.git.GitConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class GitTagTest extends GitTest {
	@Test
	public void testTag() throws Exception {

		URI workspaceLocation = createWorkspace(getMethodName());

		String projectName = getMethodName();
		JSONObject project = createProjectOrLink(workspaceLocation, projectName, gitDir.toString());

		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitTagUri = gitSection.getString(GitConstants.KEY_TAG);

		// tag HEAD with 'tag'
		JSONObject tag = tag(gitTagUri, "tag", Constants.HEAD);
		assertEquals("tag", tag.getString(ProtocolConstants.KEY_NAME));
		new URI(tag.getString(ProtocolConstants.KEY_CONTENT_LOCATION));
	}

	@Test
	public void testListTags() throws Exception {

		URI workspaceLocation = createWorkspace(getMethodName());

		String projectName = getMethodName();
		JSONObject project = createProjectOrLink(workspaceLocation, projectName, gitDir.toString());

		JSONObject gitSection = project.getJSONObject(GitConstants.KEY_GIT);
		String gitHeadUri = gitSection.getString(GitConstants.KEY_HEAD);
		String gitTagUri = gitSection.getString(GitConstants.KEY_TAG);

		JSONArray tags = listTags(gitTagUri);
		assertEquals(0, tags.length());

		// log
		WebRequest request = GitCommitTest.getGetGitCommitRequest(gitHeadUri, false);
		WebResponse response = webConversation.getResponse(request);
		assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
		JSONObject logResponse = new JSONObject(response.getText());
		JSONArray commitsArray = logResponse.getJSONArray(ProtocolConstants.KEY_CHILDREN);
		assertEquals(1, commitsArray.length());

		JSONObject commit = commitsArray.getJSONObject(0);
		String commitId = commit.getString(ProtocolConstants.KEY_NAME);
		String commitLocation = commit.getString(ProtocolConstants.KEY_LOCATION);

		tag(gitTagUri, "tag1", commitId);

		tags = listTags(gitTagUri);
		assertEquals(1, tags.length());
		assertEquals("tag1", tags.getJSONObject(0).get(ProtocolConstants.KEY_NAME));

		// update commit with tag
		tag(commitLocation, "tag2");

		tags = listTags(gitTagUri);
		assertEquals(2, tags.length());
		assertEquals("tag2", tags.getJSONObject(1).get(ProtocolConstants.KEY_NAME));
	}
}
