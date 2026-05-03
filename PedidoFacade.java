import modelos.Pedido;
import modelos.ResultadoColeta;
import modelos.ResultadoPagamento;
import modelos.ResultadoPedido;
import sistemas.EstoqueService;
import sistemas.FreteService;
import sistemas.NotificacaoService;
import sistemas.PagamentoService;

/**
 * PedidoFacade fornece uma interface unificada e simplificada
 * para o processo de finalização de pedidos, ocultando a
 * complexidade dos subsistemas de Estoque, Pagamento, Frete
 * e Notificação do código cliente.
 */
public class PedidoFacade
{
    // Os subsistemas são instanciados aqui e nunca expostos ao cliente
    private final EstoqueService     estoque     = new EstoqueService();
    private final PagamentoService   pagamento   = new PagamentoService();
    private final FreteService       frete       = new FreteService();
    private final NotificacaoService notificacao = new NotificacaoService();

    /**
     * Orquestra todos os subsistemas para finalizar um pedido.
     * O cliente chama apenas este método — toda a complexidade
     * fica encapsulada na Facade.
     */
    public ResultadoPedido finalizarPedido(Pedido pedido) 
    {
        boolean verificaEstoque = estoque.verificarDisponibilidade(pedido.produtoId, pedido.quantidade);
    
        if (!verificaEstoque) 
        {
            notificacao.enviarEmail(pedido.email, "Produto sem estoque!");
            return new ResultadoPedido(false, "Produto sem estoque!");
        }

        boolean verificaCartao = pagamento.validarCartao(pedido.dadosCartao);
        if (!verificaCartao) 
        {
            notificacao.enviarEmail(pedido.email, "Cartão inválido!");
            return new ResultadoPedido(false, "Cartao invalido!");
        }

        ResultadoPagamento resultadoPagamento = pagamento.processarCobranca(pedido.valor, pedido.dadosCartao);
        if (!resultadoPagamento.sucesso) 
        {
            notificacao.enviarEmail(pedido.email, "Pagamento recusado!");
            return new ResultadoPedido(false, "Pagamento recusado!");
        }

        estoque.reservarItens(pedido.produtoId, pedido.quantidade);

        double valorFrete = frete.calcularFrete(pedido.cep, pedido.peso);

        ResultadoColeta coleta = frete.agendarColeta(pedido.cep, resultadoPagamento.transacaoId);

        notificacao.enviarEmail(pedido.email, "Pedido finalizado com sucesso! Transação: " + resultadoPagamento.transacaoId);
        notificacao.enviarSMS(pedido.telefone, "Pedido enviado com sucesso!" + resultadoPagamento.transacaoId);

        ResultadoPedido resultado = new ResultadoPedido(true, "Pedido finalizado com sucesso!");
        resultado.transacaoId = resultadoPagamento.transacaoId;
        resultado.codigoColeta = coleta.codigo;
        resultado.prazoEntrega  = coleta.prazo;

        return resultado;
    }

    /**
     * Cancela um pedido já realizado, estornando o pagamento
     * e liberando os itens reservados no estoque.
     */
    public ResultadoPedido cancelarPedido(String produtoId, int quantidade, String transacaoId)
    {
        System.out.println("Cancelando pedido — id " + transacaoId);

        System.out.println("[Pagamento] Retornando valor! " + transacaoId);

        System.out.println("[Estoque] Repondo " + quantidade + " unidades do produto " + produtoId);

        System.out.println("[Notificacao] Notificando cliente sobre cancelamento");

        ResultadoPedido resultado = new ResultadoPedido(false, "Pedido cancelado");
        resultado.transacaoId = transacaoId;
        resultado.codigoColeta = "";
        resultado.prazoEntrega = "";
        return resultado;
    }

    /**
     * Retorna um resumo de status do pedido sem expor detalhes internos.
     */
    public String consultarStatus(String transacaoId)
    {
        return "Status do pedido: " + transacaoId + " | Processado!";
    }
    
}


