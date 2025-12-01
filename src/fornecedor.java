public class fornecedor {
    private static int ultimoID = 10_000;

    private String nome;
    private int documento;
    private ABB<Integer, Produto> produtos;

    public fornecedor(String nome) {
        if (nome == null) {
            throw new IllegalArgumentException("Nome não pode ser nulo.");
        }
        String[] partes = nome.trim().split("\\s+");
        if (partes.length < 2) {
            throw new IllegalArgumentException("Nome do fornecedor deve conter pelo menos duas palavras");
        }

        this.nome = nome;
        this.documento = ultimoID++;
        this.produtos = new ABB<>();

        }

    public String getNome() {
        return nome;
    }

    public int getDocumento() {
        return documento;
    }

    public void adicionarProduto(Produto novo) {
        if (novo == null) {
            throw new IllegalAccessException("Produto não pode ser nulo.");
        }
        int chave = novo.getidProduto();
        produtos.inserir(chave, novo);
    }

    @Override
    public String toString() {
        return "Fornecedor{" + "nome='" + nome + '\'' + ", documento=" + documento + ", produtos=" + produtos + '}';
    }

    @Override
    public int hashCode() {
        return this.documento;
    }
    }
