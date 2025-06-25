import { WebSocketServer } from 'ws';
import readline from 'readline';
import { getLocalIp } from './utils/getLocalIp.js';


const port = 6109
const wss = new WebSocketServer({ port });


const address = `ws://${getLocalIp()}:${port}`;
console.log(`🛜  Servidor WebSocket rodando em ${address}`);
console.log(`📡 Dispositivos conectados: ` + wss.clients.size)

let rl;
function startPrompt() {
    if (rl) return;

    rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });
}

function stopPrompt() {
    if (rl) {
        rl.close();
        rl = null;
    }
}

function broadcast(data) {
    const message = JSON.stringify(data);
    
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(message);
        }
    });

    console.log(`📨 Venda enviada: ` + message)
}

const paymentUpdate = () => {
  console.log('\n');
  rl.question('🟢 Digite o Valor Total: ', (valorTotalStr) => {
      const valorTotal = parseFloat(valorTotalStr);

      if (isNaN(valorTotal)) {
          console.log('❌ ValorTotal inválido. Digite um número.');
          return paymentUpdate();
      }

      rl.question('🟢 Digite o desconto: ', (descontoStr) => {
          const descontos = parseFloat(descontoStr);

          if (isNaN(descontos)) {
              console.log('❌ Desconto inválido. Digite um número.');
              return paymentUpdate();
          }

          const totalPagar = valorTotal - descontos;

          const message = {
              event: 'payment_update',
              valorTotal,
              descontos,
              totalPagar
          };

          broadcast(message);
          paymentUpdate();
      });
  });
};
wss.on('connection', (ws) => {
    console.log('\n');
    console.log('📡 Tef ProMax Connectado');
    console.log(`📡 Dispositivos conectados: ` + wss.clients.size)
    startPrompt();

    if (wss.clients.size > 0) {
        paymentUpdate()
    }

    ws.on('close', () => {
        console.log('\n');
        console.log('❌ Tef ProMax Desconectado');
        console.log(`📡 Dispositivos conectados: ` + wss.clients.size)
        
        if(wss.clients.size < 1) {
          stopPrompt
        }
    });
});
