fetch("/style-config").then(res => {
    if (res.ok) {
        return res.json();
    }
    throw new Error("Gateway down")
}).then(json => {
    if (json.stylesheet) {
        var styleSheet = document.createElement("link")
        styleSheet.setAttribute("rel", "stylesheet")
        styleSheet.setAttribute("href", json.stylesheet)
        document.head.appendChild(styleSheet)
    }
})