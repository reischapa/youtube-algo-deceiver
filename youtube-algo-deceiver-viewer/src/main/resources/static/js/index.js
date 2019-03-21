const deceiverPlayerConfig = {
  shouldAutoplay: true
};

function getIframe() {
  return document.getElementById("iframe");
}

function changeVideo(evt) {
  const iframe = getIframe();
  if (iframe.style.visibility === 'hidden') {
    iframe.style.visibility = 'visible';
  }

  const value = evt.target.value;

  iframe.src = `https://www.youtube.com/embed/${value}?enablejsapi=1`;

  if (deceiverPlayerConfig.shouldAutoplay) {
    setTimeout(() => {
        iframe.contentWindow.postMessage(JSON.stringify({
          event: 'command',
          func: 'playVideo',
          args: []
        }), '*')
    }, 1500)
  }
}

function setShouldAutoplay(e) {
  deceiverPlayerConfig.shouldAutoplay = !!e.target.checked;
}
