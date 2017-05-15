const baseUrl = 'http://123.206.108.70:666';
const navItem = `<a class="mdl-navigation__link" href="javascript: showTask('{0}')">{1}</a>`;
const listItem = '<li class="mdl-list__item mdl-list__item--three-line"><span class="mdl-list__item-primary-content">' +
    '<span>{0}</span><span class="mdl-list__item-text-body">创建日期：{1}</span></span>' +
    '<span class="mdl-list__item-secondary-content">{2}</span></li>';
const progressItem = '<span style="width: 250px" class="mdl-progress mdl-js-progress is-upgraded" data-upgraded=",MaterialProgress">' +
    '<span class="progressbar bar bar1" style="width: {0}%;"></span><span class="bufferbar bar bar2" style="width: 100%;">' +
    '</span><span class="auxbar bar bar3" style="width: 0;"></span></span>';
const completeItem = '<a style="width: 50px">已完成</a>';
const detailItem = `<a href="javascript: showDetail('{0}', '{1}')" style="width: 50px">详情</a>`;
const dataPage = '<h4>{0}</h4><p>网址：<a target="_blank" href="{1}">{1}</a></p>' +
    '<p>时间：{2}</p><p>地点：{3}</p><p>人物：{4}</p>' +
    '<p>{5}</p><p><ul>{6}</ul></p><p>详细内容：{7}</p>';
const imageItem = '<figure style="display: inline-block; margin: 0 16px 16px 0;width: 45%;"><img alt="{0}" width="100%" src="data:image/png;base64,{1}" /><figcaption>{0}</figcaption></figure>';
const commentItem = `<li>{0}（{1}）：{2} <a href="javascript: " onclick="analyze(this, '{2}')">情感分析</a></li>`;

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

function showTask(id) {
    ajax('GET', baseUrl + '/data/nlp' + id + '?size=10&page=0', function (text) {
        const r = JSON.parse(text);
        if (r.status !== 'ok') {
            console.log(r.data);
            return
        }

        let i, items = '';
        for (i = 0; i < r.data.length; i++) {
            items += format(listItem,
                r.data[i].data.summary,
                convert(r.data[i].createdAt),
                format(detailItem, id, r.data[i].url));
        }
        document.querySelector('#content-list').innerHTML = items;
    });
}

function showDetail(id, url) {
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
                img += format(imageItem, item.images[i][0],
                    btoa(String.fromCharCode.apply(null, hexStringToByteArray(item.images[i][1]))));
            }
        }

        ajax('GET', baseUrl + '/data/nlp' + id + '/' + encodeURIComponent(url), function (text) {
            const r = JSON.parse(text);
            if (r.status !== 'ok') {
                console.log(r.data);
                return
            }

            const nlp = r.data.data;
            let i, comments = '';
            for (i = 0; i < item.hotComments.length; i++) {
                comments += format(commentItem,
                    item.hotComments[i].userName,
                    item.hotComments[i].location,
                    item.hotComments[i].comment);
            }
            for (i = 0; i < item.newComments.length; i++) {
                comments += format(commentItem,
                    item.newComments[i].userName,
                    item.newComments[i].location,
                    item.newComments[i].comment);
            }
            document.querySelector('#content-list').innerHTML = format(dataPage,
                nlp.summary,
                item.url,
                item.postTime,
                nlp.places.join('，'),
                nlp.people.join('，'),
                img,
                comments,
                item.content
            );
        });
    })
}

function analyze(item, text) {
    ajax('GET', 'http://119.23.251.243/cla/' + encodeURIComponent(text), function (text) {
        const r = JSON.parse(text);
        item.innerText = r.label;
    });
}

function homepage() {
    const tasks = [];
    ajax('GET', baseUrl + '/task', function (text) {
        const r = JSON.parse(text);
        if (r.status !== 'ok') {
            console.log(r.data);
            return
        }

        let i, items = '';
        for (i = 0; i < r.data.length; i++) {
            items += format(navItem, r.data[i].id, r.data[i].name);
            tasks.push(r.data[i])
        }
        document.querySelector('#nav-list').innerHTML = items;
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
                        progress);
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
homepage();