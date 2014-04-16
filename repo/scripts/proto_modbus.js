//Modbus support
mb = {
	rtu: {
		//RTU request generator
		req: function(fun, slave, addr, regs) {
			if (fun == 3) { 
				var data = [slave, 3, addr >> 8, addr & 0xFF, regs >> 8, regs & 0xFF];
				return mb.rtu.addCRC(data);
			}
		},
		
		//RTU CRC16 calculator
		crc16: function(data) {
			var crc = 0xFFFF;
			for (var i=0; i<data.length; i++) {
				crc ^= data[i] & 0xFF;
				for (var b=0; b<8; b++) {
					var flag = crc & 1;
					crc >>= 1;
					if (flag) crc ^= 0xA001;
				}
			}
			return crc;
		},
		
		//append CRC of array
		addCRC: function(data) {
			var crc = mb.rtu.crc16(data);
			data.push(crc & 0xFF);
			data.push(crc >> 8);
			return data;
		}
	}
};

