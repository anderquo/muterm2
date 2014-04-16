//RX filter.
//in: array of bytes in global variable bytes_in, out: resulting string of HEX bytes.
filters.i.all.hex = {
    enabled: true,
    
    name:  "HEX",
    descr: "HEX string",
    
    pageType: "text",

    cols: 16, //width, columns
    col:   0, //column counter
    
    //initialize
    init: function() {
        this.col = 0;
    },
    
    //parse bytes_in
    update: function() {
        if (filters.i.all.hex.enabled) {
            var s = [];
            for (var b in bytesIn) {
                s.push(tools.b2h(bytesIn[b]));
                s.push(' ');
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
            get: function()  { return filters.i.all.hex.enabled; },
            set: function(b) { filters.i.all.hex.enabled = (b == true); }
        },

        clear: { type:'button', name:'Clear', descr:'Clear all text',
            update: function() { filters.i.all.hex.textObj.clear(); }
        },
        
        cols: { type:'int', name:'Cols', descr:'Number of HEX columns', width:3,
            get: function()  { return filters.i.all.hex.cols; },
            set: function(n) { filters.i.all.hex.cols = parseInt(n); }
        }
    }
};

