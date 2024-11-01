<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link href="${mvc.basePath}/../app.css" rel="stylesheet">
<title>Jukdoc</title>
</head>
<body
	x-data="{ paragraphs: [] }"
	x-init="$get('/paragraphs', { error: 'Cannot get paragraphs' }).then(res => { if (res.status==200) paragraphs = res.data })">

	<h1>Original Document</h1>

	<div style="color: red" x-show="$store.errors.length > 0">
		<template x-for="error in $store.errors">
			<div x-text="error"></div>
		</template>
	</div>

	<div x-data="{ markAsRead(tag) { (paragraphs.find(para => para.positionTag === tag)).read = true; } }">
	    <template x-for="para in paragraphs">
	        <div :class="(para.read ? 'read' : 'unread') + ' ' + (para.header ? 'header' : 'paragraph')">
	        	<button @click="markAsRead(para.positionTag)">MarkAsRead</button>
			    <span x-text="para.paragraph"></span>(<span x-text="para.positionTag"></span>)
		    </div>
	    </template>
	</div>

	<script type="module">
		import rest from '${mvc.basePath}/../rest.js';
		rest.start('${mvc.basePath}/api', '${mvc.csrf.token}');
	</script>
</body>
</html>