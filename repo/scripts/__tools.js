tools = {
    //convert byte to HEX string
    b2h: function(b) {
        return (b < 16) ? '0' + b.toString(16) : b.toString(16);
    },
    
    //convert string to array of bytes
    s2b: function(s) {
        var b = [];
        for (var i=0; i<s.length; i++) b.push(s.charCodeAt(i));
        return b;
    },
    
    //convert array of bytes to string
    b2s: function(b) {
        var s = [];
        for (var i=0; i<b.length; i++) s.push(String.fromCharCode(b[i]));
        return s.join('');
    }
};

