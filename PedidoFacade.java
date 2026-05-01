public class PedidoFacade {

    private Estoque estoque;
    private Pagamento pagamento;
    private Frete frete;
    private Notificacao notificacao;

    public PedidoFacade() 
    {
        this.estoque = new Estoque();
        this.pagamento = new Pagamento();
        this.frete = new Frete();
        this.notificacao = new Notificacao();
    }

    // Método principal
    public void finalizarPedido (Pedido pedido)
    {

        if (!estoque.verificarDisponibilidade(pedido.produtoId, pedido.quantidade)) 
        {
            notificacao.enviarFalha(pedido.email, "Produto sem estoque.");
            return;
        }

        estoque.reservarProduto(pedido.produtoId, pedido.quantidade);

        if (!pagamento.processarPagamento(pedido.valor)) 
        {
            notificacao.enviarFalha(pedido.email, "Pagamento recusado.");
            return;
        }

        String calculaFrete = frete.calcularFrete(pedido.endereco);
        System.out.println(calculaFrete);
        frete.gerarEnvio(pedido.endereco);

        notificacao.enviarConfirmacao(pedido.email);

        System.out.println("Pedido finalizado com sucesso!");
    }

    public void cancelarPedido(Pedido pedido) 
    {
        System.out.println("Cancelando pedido...");
        notificacao.enviarFalha(pedido.email, "Pedido cancelado.");
    }

    public void consultarStatus(Pedido pedido)
    {
        System.out.println("Processando o pedido com ID: "+pedido.produtoId);
    }
}