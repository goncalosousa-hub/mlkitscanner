var exec = require('cordova/exec');

var MLKitScanner = {
    // Função para iniciar o scanner
    scanDocument: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'MLKitScanner', 'scanDocument', []);
    }
};

module.exports = MLKitScanner;