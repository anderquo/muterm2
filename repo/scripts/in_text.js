//RX filter.
//in: array of bytes in global variable bytes_in, out: ASCII filter.
filters.i.all.text = {
    enabled:  true,

    tag:      'text',
    name:     'Text',
    descr:    'ASCII text',
    
    pageType: 'text',

    cols:     0, //width, columns
    col:      0, //column counter
    
    //initialize
    init: function() {
        this.col = 0;
    },
    
    //parse bytes_in
    update: function() {
        if (filters.i.all.text.enabled) {
            var s = [];
            for (var b in bytesIn) {
                if ((this.cols > 0)&&(++this.col >= this.cols)) {
                    this.col = 0;
                    s.push('\n');
                }
                s.push(String.fromCharCode(bytesIn[b]));
            }
            s = s.join('');
            this.textObj.appendText(s);
            return s;
        } else return "";
    },
    
    props: {
        enabled: { type:'bool', name:'Enabled', descr:'Enable text processing',
            get: function()  { return filters.i.all.text.enabled; },
            set: function(b) { filters.i.all.text.enabled = (b == true); }
        },
        
        clear: { type:'button', name:'Clear', descr:'Clear all text',
            update: function() { filters.i.all.text.textObj.clear(); }
        },

        cols: { type:'int', name:'Width', descr:'Number of char columns', width:3,
            get: function()  { return filters.i.all.text.cols; },
            set: function(n) { filters.i.all.text.cols = parseInt(n); }
        }
    }
};

