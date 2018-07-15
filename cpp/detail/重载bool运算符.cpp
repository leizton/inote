class Test {
private:
    int v_;
public:
    Test(int v) :v_(v) {}
    operator bool() { return v_ != 0; }
};

int main() {
    Test t0(0);
    cout << t0 ? "true" : "false" << endl;  // false
    Test t1(1);
    cout << t1 ? "true" : "false" << endl;  // true
    return 0;
}