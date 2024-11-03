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
      x-data="{ paragraphs: [], history: [] }"
      x-init="$get('/paragraphs', { error: 'Cannot get paragraphs' })
                 .then(res => res.status==200 ? paragraphs = res.data : null);
              $get('/chat/opening-words', { error: 'Cannot get opening words' })
                 .then(res => res.status == 200 ? history.push(res.data) : null);
">

<div class="title-area">
    Jukdoc
</div>
<div class="main-content">
    <div class="document-column">
        <h1>Original Document</h1>

        <div x-data="{ markAsRead(tag) { (paragraphs.find(para => para.positionTag === tag)).read = true; } }">
            <template x-for="para in paragraphs">
                <div :class="(para.read ? 'read' : 'unread') + ' ' + (para.header ? 'header' : 'paragraph')"
                     :id="para.positionTag.replaceAll(/#/g, '-')">
                    <button @click="markAsRead(para.positionTag)">MarkAsRead</button>
                    <span x-text="para.paragraph"></span>(<span x-text="para.positionTag"></span>)
                </div>
            </template>
        </div>
    </div>

    <div class="chat-column">
        <div class="chat-history">
            <div style="color: red" x-show="$store.errors.length > 0">
                <template x-for="error in $store.errors">
                    <div x-text="error"></div>
                </template>
            </div>

            <template x-for="msg in history">
                <div :class="'message-rect ' + (msg.speaker === 'AI' ? 'from-ai' : 'from-user')">
                    <div class="chat-message" x-html="msg.message"></div>
                    <div class="chat-refs" x-show="msg.speaker === 'AI'">
                    <template x-for="refStr in msg.refs">
                        <span class="ref"
                             x-data="{ refArr: refStr.split(':') }"
                             @click="location.href='#'+refArr[1].replaceAll(/#/g, '-')"
                             x-text="'[*' + refArr[0] + '] ' + refArr[2]">
                        </span>
                    </template>
                    </div>
                </div>
            </template>
        </div>
        <form class="chat-input-container"
              x-data="{ param: { message: '' } }"
              @submit.prevent="
              history.push({ speaker: 'User', message: param.message, refs: [] });
              $post('/chat/query', { param, error: 'Cannot send message' })
              .then(res => res.status == 200 ? history.push(res.data) : null);
              param.message = '';"
        >
            <input type="text" x-model="param.message" class="chat-input" placeholder="Enter your message..."/>
            <button class="send-button">Send</button>
        </form>
    </div>

</div>

<script type="module">
    import rest from '${mvc.basePath}/../rest.js';
    rest.start('${mvc.basePath}/api', '${mvc.csrf.token}');
</script>
</body>
</html>