import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;
    
    static ABB<Integer, Produto> produtosCadastradosPorId;
    
    private static AVL<Integer, fornecedor> arvoreFornecedores;
    private static TabelaHash<Produto, List<fornecedor>> fornecedoresPorProduto;
    private static List<Produto> catalogoProdutos = new ArrayList<>();


    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
   
    public static <K> AVL<K, fornecedor> LerFornecedores(String nomeArquivoDados, Function<fornecedor, K> extratorDeChave) {
        AVL<K, fornecedor> arvore = new AVL<> ();
    }

    try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivoDados))) {
        String linha = br.readLine();
        if (linha == null) {
            throw new IOException("Arquivo vazio: " + nomeArquivoDados);
        }

        int quantidade = Integer.parseInt(linha.trim());
        
        for (int i = 0; i < quantidade; i++) {
            String nomeFornecedor = br.readLine();

            if (nomeFornecedor == null || nomeFornecedor.trim().isEmpty()) {
                break;
            }

            fornecedor fornecedor = new fornecedor(nomeFornecedor.trim());

            List<Produto> selecionados = selecionarProdutosAleatorios(catalogoProdutos, 6);

            for (Produto p : selecionados) {
                fornecedor.adicionarProduto(p);
                associarProdutoAFornecedor(p, fornecedor);
            }
            K chave = extratorDeChave.apply(fornecedor);

            arvore.inserir(chave, fornecedor);
        }
    } catch (IOException e) {
        System.err.println("Problema ao ler o arquivo de fornecedores: " + e.getMessage());
    } return arvore; }

    public static void associarProdutoAFornecedor(Produto produto, fornecedor fornecedor) {
        List<fornecedor> lista = fornecedoresPorProduto.obter(produto);

        if (lista == null) {
            lista = new ArrayList<>();
            fornecedoresPorProduto.inserir(produto, lista);
        }

        if (!lista.contains(fornecedor)) {
            lista.add(fornecedor);
        }
    }

    public static String relatorioDeFornecedor(int documento) {
        if (arvoreFornecedores == null) {
            return "Árvore de fornecedores ainda não foi inicializada.";
        }
        fornecedor fornecedor = arvoreFornecedores.buscar(documento);

        if (fornecedor == null) {
            return "Fornecedor com documento " + documento + "não encontrado.";
        }
        return fornecedor.toString();
    }

    public static void fornecedoresDoProduto(int codigoProduto, String nomeArquivoSaida) {
        if (fornecedoresDoProduto == null) {
            System.err.println("Tabela de fornecedores por produto ainda não foi inicializada.");
            return;
        }

        Produto escolhido = buscarProdutoPorCodigo(codigoProduto);

        if (escolhido == null) {
            System.err.println("Produto com código " + codigoProduto + " não encontrado.");
            return;
        }

        List<fornecedor> Lista = fornecedoresPorProduto.obter(escolhido);

        if (lista == null || lista.isEmpty()) {
            System.err.println("Não há fornecedores cadastrados para o produto " + codigoProduto + ".");
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nomeArquivoSaida))) {
            bw.write("Relatório de fornecedores do produto " + escolhido);
            bw.newLine();
            bw.newLine();

            for (fornecedor f : lista) {
                bw.write (f.toString());
                bw.newLine();
            }
            System.out.println("Relatório gerado em: " + nomeArquivoSaida);

        }   catch (IOException e) {
            System.err.println("Erro ao escrever arquivo de relatório " + e.getMessage());
        }
    }

    private static Produto buscaProdutoPorCodigo (int codigo) {
        for (Produto p : catalogoProdutos) {
            if (p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Carregar produtos por nome/descrição");
        System.out.println("3 - Carregar produtos por id");
        System.out.println("4 - Procurar produto, por nome");
        System.out.println("5 - Procurar produto, por id");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma árvore de produtos. Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas de leitura.
     */
    static <K> ABB<K, Produto>Produtos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
    	
    	Scanner arquivo = null;
    	int numProdutos;
    	String linha;
    	Produto produto;
    	ABB<K, Produto> produtosCadastrados;
    	K chave;
    	
    	try {
    		arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
    		
    		numProdutos = Integer.parseInt(arquivo.nextLine());
    		produtosCadastrados = new ABB<K, Produto>();
    		
    		for (int i = 0; i < numProdutos; i++) {
    			linha = arquivo.nextLine();
    			produto = Produto.criarDoTexto(linha);
    			chave = extratorDeChave.apply(produto);
    			produtosCadastrados.inserir(chave, produto);
    		}
    		quantosProdutos = numProdutos;
    		
    	} catch (IOException excecaoArquivo) {
    		produtosCadastrados = null;
    	} finally {
    		arquivo.close();
    	}
    	
    	return produtosCadastrados;
    }
    
    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
    	
    	Produto produto;
    	
    	cabecalho();
    	System.out.println("Localizando um produto...");
    	
    	try {
    		produto = produtosCadastrados.pesquisar(procurado);
    	} catch (NoSuchElementException excecao) {
    		produto = null;
    	}
    	
    	System.out.println("Número de comparações realizadas: " + produtosCadastrados.getComparacoes());
    	System.out.println("Tempo de processamento da pesquisa: " + produtosCadastrados.getTempo() + " ms");
        
    	return produto;
    	
    }
    
    /** Localiza um produto na árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
     *  Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        
        // TODO
    	return null;
    }
    
    /** Localiza um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        
    	// TODO
    	return null;
    }
    
    private static void mostrarProduto(Produto produto) {
    	
        cabecalho();
        String mensagem = "Dados inválidos para o produto!";
        
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        
        System.out.println(mensagem);
    }
    
    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static <K> void listarTodosOsProdutos(ABB<K, Produto> produtosCadastrados) {
    	
        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        System.out.println(produtosCadastrados.toString());
    }
    
	public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos(produtosCadastradosPorNome);
                case 2 -> produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao));
                case 3 -> produtosCadastradosPorId = lerProdutos(nomeArquivoDados, (p -> p.idProduto));
                case 4 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 5 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
            }
            pausa();
        }while(opcao != 0);       

        teclado.close();   
        
        arvoreFornecedores = new AVL<>();
        fornecedoresPorProduto = new TabelaHash<>();
    }
}
