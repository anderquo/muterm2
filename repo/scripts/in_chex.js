//RX filter.
//in: array of bytes in global variable bytes_in, out: resulting string of HEX bytes in C array format.
filters.i.all.chex = {
    enabled:  true,

    name:     'C-HEX',
    descr:    'C-HEX array',

    pageType: 'text',

    cols:     16, //width, columns
    col:      3,  //column counter
    
    //initialize
    init: function() {
        this.col = 0;
    },
    
    //parse bytes_in
    update: function() {
        if (filters.i.all.chex.enabled) {
            var s = [];
            for (var b in bytesIn) {
                s.push('0x');
                s.push(tools.b2h(bytesIn[b]));
                s.push(',');
                this.col += 1;
                if (this.col >= this.cols) {
                    this.col = 0;
                    s.push('\n');
                }
            }
            s = s.join('');
            this.textObj.appendText(s);
            return s;
        } else return "";
    },

    props: {
        enabled: { type:'bool', name:'Enabled', descr:'Enable text processing',
            get: function()  { return filters.i.all.chex.enabled; },
            set: function(b) { filters.i.all.chex.enabled = (b == true); }
        },

        clear: { type:'button', name:'Clear', descr:'Clear all text',
            update: function() { filters.i.all.chex.textObj.clear(); }
        },
        
        cols: { type:'int', name:'Cols', descr:'Number of HEX columns', width:3,
            get: function()  { return filters.i.all.chex.cols; },
            set: function(n) { filters.i.all.chex.cols = parseInt(n); }
        }
    }
};

