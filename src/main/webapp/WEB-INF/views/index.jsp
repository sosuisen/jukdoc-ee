<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link href="${mvc.basePath}/../app.css" rel="stylesheet">
    <title>Jukdoc</title>
</head>
<body class="container"
      x-data="{ paragraphs: [] }"
      x-init="$get('/paragraphs', { error: 'Cannot get paragraphs' }).then(res => { if (res.status==200) paragraphs = res.data })">

<div class="title-area">
    Jukdoc
</div>
<div class="main-content">
    <div class="document-column">
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
    </div>

    <div class="chat-column">
        <div class="chat-area">

        </div>
        <div class="chat-input-container">
            <input type="text" class="chat-input" placeholder="Enter your message..."/>
            <button class="send-button">Send</button>
        </div>
    </div>

</div>

<script type="module">
    import rest from '${mvc.basePath}/../rest.js';

    rest.start('${mvc.basePath}/api', '${mvc.csrf.token}');
</script>
</body>
</html>