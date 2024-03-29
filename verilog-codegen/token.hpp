#ifndef VERILOG_CODGEN_TOKEN_INCLUDED
#define VERILOG_CODGEN_TOKEN_INCLUDED

#include "tree.hpp"

#include <unordered_set>
#include <stdexcept>

/**
 * A Token is the smallest unit in our Verilog syntax Tree
 * A Token can be "reserved" and trying to create another token with the same
 * name will result in an exception, this is to prevent the use of keywords as 
 * identifiers (and force the usage of reseved keyword/operator without 
 * recreating them mutiple times)
 * 
*/
class Token : public Tree {
public:
    Token(std::string value, bool reserve);
    ~Token();

    std::string to_string(int indent = 0);
private:
    std::string value;

    static std::unordered_set<std::string> tokens;
};

/**
 * Declare reserved Token for (most) verilog keywords and operators
 * Tokens that conflict with a c++ keyword add a "Kw" suffix
*/

// --- Keywords

extern Token always;
extern Token begin;
extern Token elseKw;
extern Token end;
extern Token endmodule;
extern Token ifKw;
extern Token input;
extern Token inout;
extern Token module;
extern Token output;
extern Token reg;
extern Token wire;

// --- Operators and special characters

extern Token coma;
extern Token lPar;
extern Token rPar;
extern Token lBracket;
extern Token rBracket;
extern Token colon;
extern Token eol;
extern Token ws;

#endif