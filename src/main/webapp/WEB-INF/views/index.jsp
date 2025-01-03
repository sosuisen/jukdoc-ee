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
      x-data="{ paragraphs: [], history: [], suggestions: [],
          scrollToBottom() { setTimeout(() => $refs.chatHistory.scrollTop = $refs.chatHistory.scrollHeight, 10) },
          post(param) {
              $post('/chat/query', { param, error: 'Cannot send message' })
                   .then(res => {
                        if(res.status == 200){
                            this.history.pop();
                            this.history.push(res.data);
                            this.suggestions = res.data.suggestions;
                            res.data.refs.forEach(refStr =>
                                this.paragraphs.find(para => para.positionTag === refStr.split(':')[1]).read = true
                            );
                            this.scrollToBottom();
                        }
                   });
          }
       }"
      x-init="$get('/document', { error: 'Cannot get paragraphs' })
                 .then(res => res.status==200 ? paragraphs = res.data : null);
              $get('/chat/opening-words', { error: 'Cannot get opening words' })
                 .then(res => res.status == 200 ? (history.push(res.data), suggestions = res.data.suggestions) : null);
">

<div class="title-area">
    <div class="title">Jukdoc</div>
    <div class="reading-completion-rate"
         x-data="{ rate: 0 }"
         x-effect="rate = paragraphs.length > 0 ? Math.round(paragraphs.filter(para => para.read && !para.header).length / paragraphs.filter(para => !para.header).length * 100) : 0">
        <span class="rate-title">Reading completion rate:</span>
        <span class="rate-value" x-text="rate + '%'"></span>
    </div>
    <div class="user-controls">
        <div class="username">User: ${userName}</div>
        <div class="delete-all-record">
            <button @click="$delete('/document/reading-record', { error: 'Cannot delete all records' })
                            .then(res => res.status == 200 ? paragraphs.forEach(para => para.read = false) : null)">
                Delete All Records
            </button>
        </div>
    </div>
</div>
<div class="main-content">
    <div class="document-column">
        <h1>Original Document</h1>

        <div x-data="{ readParagraph(tag) {
          const param = { message: 'Read this paragraph.', positionTag: tag };
          history.push({ speaker: 'User', message: param.message, refs: [] });
          history.push({ speaker: 'AI', message: '...', refs: [] });
          scrollToBottom();
          post(param);
         }
        }">
            <template x-for="para in paragraphs">
                <div :class="(para.read ? 'read' : 'unread') + ' ' + (para.header ? 'header' : 'paragraph')"
                     :id="para.positionTag">
                    <span x-text="para.paragraph"></span>
                    <button x-show="!para.header" @click="readParagraph(para.positionTag)">Read</button>
                </div>
            </template>
        </div>
    </div>

    <div class="chat-column">
        <div class="chat-history" x-ref="chatHistory">
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
                              @click="location.href='#'+ refArr[1]; highlightElement('#' + refArr[1]);"
                              x-text="'[*' + refArr[0] + '] ' + refArr[2]">
                        </span>
                        </template>
                    </div>
                </div>
            </template>
        </div>
        <div class="chat-input-container"
             x-data="{
             param: { message: '' },
             request() {
                    if (this.param.message.trim() === '') return;
                    history.push({ speaker: 'User', message: this.param.message, refs: [] });
                    history.push({ speaker: 'AI', message: '...', refs: [] });
                    scrollToBottom();
                    post(this.param);
                    this.param.message = '';
                 }
             }"
        >
            <form class="chat-form" @submit.prevent="request()">
                <input type="text" x-model="param.message" class="chat-input" placeholder="Enter your message..."/>
                <button class="send-button">Send</button>
            </form>

            <div class="suggestions">
                <template x-for="suggest in suggestions">
                    <button class="suggestion-button" x-text="suggest"
                            @click="param.message = $el.innerText; request();">What is JavaScript?
                    </button>
                </template>
            </div>
        </div>
    </div>

</div>

<script type="module">
    import rest from '${mvc.basePath}/../rest.js';

    rest.start('${mvc.basePath}/api', '${mvc.csrf.token}');
</script>
<script>
    let currentHighlightHash = null;

    function highlightElement(hash) {
        const element = document.querySelector(hash);
        if (element) {
            if (currentHighlightHash) {
                const currentElement = document.querySelector(currentHighlightHash);
                if (currentElement)
                    currentElement.classList.remove("highlight");
            }
            element.classList.add("highlight");
            currentHighlightHash = hash;
        }
    }
</script>
</body>
</html>