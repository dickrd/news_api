const baseUrl = 'http://127.0.0.1:666';

const listItem = '<li class="mdl-list__item mdl-list__item--three-line">' +
    '<span class="mdl-list__item-primary-content">' +
    `<span style="cursor: pointer" onclick="showTask('{3}', {4});">{0}</span>` +
    '<span class="mdl-list__item-text-body">任务创建日期：{1}</span>' +
    '</span>' +
    '<span class="mdl-list__item-secondary-content">{2}</span>' +
    '</li>';

const progressItem = '<span style="width: 250px" class="mdl-progress mdl-js-progress is-upgraded" data-upgraded=",MaterialProgress">' +
    '<span class="progressbar bar bar1" style="width: {0}%;"></span><span class="bufferbar bar bar2" style="width: 100%;">' +
    '</span><span class="auxbar bar bar3" style="width: 0;"></span></span>';
const completeItem = '<a style="width: 50px">已完成</a>';

const newsListItem = '<li class="mdl-list__item mdl-list__item--three-line">' +
    '<span class="mdl-list__item-primary-content">' +
    `<span style="cursor: pointer" onclick="showDetail('{1}', '{2}');">{0}</span>` +
    '<span class="mdl-list__item-text-body">发布日期：{3}</span>' +
    '</span>' +
    '<span class="mdl-list__item-secondary-content">{4}</span>' +
    '</li>';
const detailItem = `<a href="{0}">{1}</a>`;

const pagationItem = '<div class="pagination">' +
    `<a href="javascript: showTask('{0}', {1}, {2})" class="">&laquo;</a>` +
    '{4}' +
    `<a href="javascript: showTask('{0}', {1}, {3})" class="">&raquo;</a>` +
    '</div>';
const pageItem = `<a href="javascript: showTask('{0}', {1}, {2})" class="{4}">{3}</a>`;

const dataPage = '<div style="padding: 0 20px;">' +
    '<h4>{0}</h4>' +
    '<p>网址：<a target="_blank" href="{1}">{1}</a></p>' +
    '<p>时间：{2}</p>' +
    '<p>{3}</p>' +
    '<p><ul>{4}</ul></p>' +
    '<p>详细内容：{5}</p>' +
    '</div>';
const imageItem = '<figure style="display: inline-block; margin: 0 16px 16px 0;width: 45%;"><img alt="{0}" width="100%" src="data:image/png;base64,{1}" /><figcaption>{0}</figcaption></figure>';
const commentItem = `<li>{0}（{1}）：{2}</li>`;

function format(format) {
    const args = Array.prototype.slice.call(arguments, 1);
    return format.replace(/{(\d+)}/g, function (match, number) {
        return typeof args[number] !== 'undefined'
            ? args[number]
            : match
            ;
    });
}

function convert(timestamp){
    const datetime = new Date(timestamp);
    const year = datetime.getFullYear();
    const month = datetime.getMonth() + 1;
    const date = datetime.getDate();

    let hour = datetime.getHours();
    let min = datetime.getMinutes();
    let sec = datetime.getSeconds();
    if (hour < 10)
        hour = '0' + hour;
    if (min < 10)
        min = '0' + min;
    if (sec < 10)
        sec = '0' + sec;

    return year + '年' + month + '月' + date + '日 ' + hour + ':' + min + ':' + sec;
}

function ajax(method, url, callback, data, extra) {
    try {
        const x = new XMLHttpRequest();
        x.open(method, url);
        x.setRequestHeader('Content-type', 'application/json');
        x.onreadystatechange = function () {
            x.readyState > 3 && callback && callback(x.responseText, x, extra);
        };
        x.send(data);
    } catch (e) {
        console.log(e);
    }
}

function showTask(id, numPages, pageNum=0, push=true) {
    ajax('GET',
        baseUrl + '/data/' + id + '?size=10&fields=url&fields=data.content&fields=data.postTime&page=' + pageNum,
        function (text) {
        const r = JSON.parse(text);
        if (r.status !== 'ok') {
            console.log(r.data);
            return
        }

        let i, items = '';
        for (i = 0; i < r.data.length; i++) {
            let postTime = r.data[i].data.postTime;
            if (!postTime)
                postTime = "未知";
            items += format(newsListItem,
                r.data[i].data.content.substring(0, 50) + "...",
                id,
                r.data[i].url,
                postTime,
                format(detailItem, r.data[i].url, r.data[i].url.split('/')[2]));
        }
        if (numPages > 1) {
            let i, pages = '';
            let startPage = pageNum > 2 ? pageNum - 2 : 0;
            for (i = 0; i < 6; i++) {
                if (startPage >= numPages)
                    break;
                if (startPage === pageNum)
                    pages += format(pageItem,
                    id,
                    numPages,
                    pageNum,
                    pageNum + 1,
                    'active');
                else
                    pages += format(pageItem,
                        id,
                        numPages,
                        startPage,
                        startPage + 1,
                        '');
                startPage++;
            }
            items += format(pagationItem,
            id,
            numPages,
            pageNum === 0 ? 0 : pageNum - 1,
            pageNum === numPages - 1 ? pageNum : pageNum + 1,
            pages);
        }

        if (push) {
            const state = {
                page: 'task',
                taskId: id,
                pageNum: pageNum,
                numPages: numPages
            };
            history.pushState(state,
                "task",
                "?taskId=" + id + "&numPages=" + numPages + "&pageNum=" + pageNum
            );
        }
        document.querySelector('#content-list').innerHTML = items;
        document.querySelector('#content').scrollIntoView();
    });
}

function showDetail(id, url, push=true) {
    ajax('GET', baseUrl + '/data/' + id + '/' + encodeURIComponent(url), function (text) {
        const r = JSON.parse(text);
        if (r.status !== 'ok') {
            console.log(r.data);
            return
        }

        const item = r.data.data;

        let i, img = '';
        if (item.images) {
            for (i = 0; i < item.images.length; i++) {
                if (item.images[i][1] === null)
                    continue;
                img += format(imageItem, item.images[i][0],
                    btoa(String.fromCharCode.apply(null, hexStringToByteArray(item.images[i][1]))));
            }
        }

        let comments = '';
        for (i = 0; i < item.hotComments.length; i++) {
            let userName = item.hotComments[i].userName;
            if (!userName)
                userName = "未知用户";
            let location = item.hotComments[i].location;
            if (!location)
                location = "未知来源";

            comments += format(commentItem,
                userName,
                location,
                item.hotComments[i].comment);
        }
        if (item.newComments) {
            for (i = 0; i < item.newComments.length; i++) {
                let userName = item.newComments[i].userName;
                if (!userName)
                    userName = "未知用户";
                let location = item.newComments[i].location;
                if (!location)
                    location = "未知来源";

                comments += format(commentItem,
                    userName,
                    location,
                    item.newComments[i].comment);
            }
        }

        if (push) {
            const state = {
                page: 'detail',
                taskId: id,
                url: url
            };
            history.pushState(state,
                "detail",
                "?taskId=" + id + "&url=" + encodeURIComponent(url)
            );
        }

        let postTime = item.postTime;
        if (!postTime)
            postTime = "未知";
        document.querySelector('#content-list').innerHTML = format(dataPage,
            item.content.substring(0, 50) + "...",
            item.url,
            postTime,
            img,
            comments,
            item.content
        );
    })
}

function homepage(push=true) {
    const tasks = [];
    ajax('GET', baseUrl + '/task', function (text) {
        const r = JSON.parse(text);
        if (r.status !== 'ok') {
            console.log(r.data);
            return
        }

        let i;
        for (i = 0; i < r.data.length; i++) {
            tasks.push(r.data[i])
        }
        if (push)
            history.pushState(null, "homepage", "?");
        document.querySelector('#content-list').innerHTML = '';

        for (i = 0; i < tasks.length; i++) {
            ajax('GET',
                baseUrl + '/task/' + tasks[i].id + '/status',
                function (text, _, extra) {
                    const r = JSON.parse(text);
                    if (r.status !== 'ok') {
                        console.log(r.data);
                        return
                    }

                    tasks[extra].remainingUrlCount = r.data.remainingUrlCount;
                    tasks[extra].remainingSourceCount = r.data.remainingSourceCount;
                    tasks[extra].dataCount = r.data.dataCount;

                    let progress = tasks[extra].dataCount /
                        (tasks[extra].remainingSourceCount + tasks[extra].remainingUrlCount + tasks[extra].dataCount);
                    if (progress === 1)
                        progress = completeItem;
                    else
                        progress = format(progressItem, progress * 100);

                    const htmlString = format(listItem,
                        tasks[extra].name,
                        convert(tasks[extra].createdAt),
                        progress,
                        r.data.id,
                        Math.ceil(r.data.dataCount / 10));
                    const div = document.createElement('div');
                    div.innerHTML = htmlString;

                    document.querySelector('#content-list').appendChild(div.firstChild);
                },
                '',
                i);
        }
    });
}

window.paceOptions = {
    restartOnRequestAfter: 1,
    ajax: {
        //ignoreURLs: ['cla', /cla/]
    }
};

window.addEventListener('popstate', function(e) {
    const data = e.state;
    if (data === null) {
        homepage(false);
    }
    else if (data.page === 'task') {
        showTask(data.taskId, data.numPages, data.pageNum, false);
    }
    else if (data.page === 'detail') {
        showDetail(data.taskId, data.url, false);
    }
});

let queryString = window.location.search.substring(1);
if (queryString.includes('&pageNum=')) {
    const queries = queryString.split('&');
    showTask(queries[0].split('=')[1], parseInt(queries[1].split('=')[1]), parseInt(queries[2].split('=')[1]), false);
}
else if (queryString.includes('&url=')) {
    const queries = queryString.split('&');
    showDetail(queries[0].split('=')[1], decodeURIComponent(queries[1].split('=')[1]), false);
}
else {
    homepage(false);
}
