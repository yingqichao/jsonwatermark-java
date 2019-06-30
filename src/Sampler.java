import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Sampler {

    private int PRNG_A = 16807;
    private int PRNG_M = (1 << 31) - 1;
    private int PRNG_MAX_RAND = PRNG_M - 1;

    private int K;
    private double delta;
    private double c;
    private int state;
    private double[] cdf;

    public Sampler(int K, double delta, double c) {
        //Seed is set by interfacing code using set_seed
        this.K = K;
        this.delta = delta;
        this.c = c;
        this.cdf = gen_rsd_cdf(K, delta, c);
    }

    public double[] gen_tau(double S, int K, double delta) {
        //The Robust part of the RSD, we precompute an array for speed

        int pivot = (int)Math.floor(K / S);
        double[] res = new double[pivot+((pivot<K)?(K-pivot):0)];

        for(int i=1;i<pivot;i++)  res[i-1] = S / K * 1 / i;
        res[pivot-1] = S/K * Math.log(S/delta);
        for(int i=pivot;i<K;i++)  res[pivot+i-pivot] = 0;

        return res;
    }

    public double[] gen_rho(int K) {
        //The Ideal Soliton Distribution, we precompute an array for speed
        double[] res = new double[K];
        res[0] = 1.0/K;
        for(int i=2;i<K+1;i++)  res[i-1] = 1.0/(i*(i-1));
        return res;
    }

    public double[] gen_mu(int K, double delta,double c){
        //The Robust Soliton Distribution on the degree of transmitted blocks
        double S = c * Math.log(K/delta) * Math.sqrt(K);
        double[] tau = gen_tau(S, K, delta);
        double[] rho = gen_rho(K);
        double normalizer = 0;
        for(double in:tau)   normalizer+=in;
        for(double in:rho)   normalizer+=in;

        double[] res = new double[K];

        for(int i=0;i<res.length;i++)   res[i] = (rho[i] + tau[i])/normalizer;

        return res;
    }

    public double[] gen_rsd_cdf(int K, double delta,double c) {
        double[] mu = gen_mu(K, delta, c);
        double[] res = new double[K];
        double sum = 0;
        for(int i=0;i<K;i++){
            sum += mu[i];res[i] = sum;
        }
        return res;
    }

    public int get_next(){
        //Executes the next iteration of the PRNG evolution process, and returns the result
        this.state = PRNG_A * this.state % PRNG_M;
        return this.state;
    }

    public int sample_d(){
        //Samples degree given the precomputed distributions above and the linear PRNG output
        double p = this.get_next() / PRNG_MAX_RAND;
        int index = 0;
        for(double v:this.cdf) {
            if (v > p)
                return index + 1;
            index ++;
        }
        return index + 1;
    }

    public void setSeed(int seed){ this.state = seed;}

    public List<Integer> get_src_blocks(Integer seed){
//        Returns the indices of a set of `d` source blocks
//        sampled from indices i = 1, ..., K-1 uniformly, where
//        `d` is sampled from the RSD described above.
        List<Integer> res = new LinkedList<>();
        if(seed!=null)  this.state = seed;
        int blockseed = this.state;
        int d = sample_d();
        int have = 0;
        Set<Integer> nums = new HashSet<>();
        while (have < d) {
            int num = get_next() % this.K;//#Use Pseudo Random 2 nd
            if(!nums.contains(num)) {
                nums.add(num);
                have += 1;
            }
        }
        res.add(blockseed);res.add(d);
        res.addAll(nums);

        return res;

    }
}
