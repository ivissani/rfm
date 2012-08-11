#include <vector>

namespace Minisat {

class ClauseInfo {
	public:
        int lbd;
        double activity;
        std::vector<int> literals;

	int getLBD() { return lbd; }
	double getActivity() { return activity; }
	std::vector<int> & getLiterals() { return literals; }
};

}
