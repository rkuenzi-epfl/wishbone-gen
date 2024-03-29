#include "token.hpp"

std::unordered_set<std::string> Token::tokens;

Token::Token(std::string value, bool reserve): value(std::move(value)) {
    // The program should fail if we try to reuse a reseved token
    if(Token::tokens.find(value) != Token::tokens.end()) {
       throw std::runtime_error("Reusing a reserved token (keyword or operator) is not allowed");
    }
    if(reserve){
        Token::tokens.insert(this->value);
    }
}

Token::~Token(){
    if(Token::tokens.find(value) != Token::tokens.end()) {
        Token::tokens.erase(value);
    }
}

std::string Token::to_string(int indent) {
    return INDENT_STR(indent) + value;
}

/**
 * Instantiate reserved Token for (most) verilog keywords and operators
 * 
*/

// Shortcut to initialize keywords
#define KW(STR) Token STR(#STR, true);

// --- Keywords

KW(always)
KW(begin)
Token elseKw("else", true);
KW(end)
KW(endmodule)
Token ifKw("if", true);
KW(input)
KW(inout)
KW(module)
KW(output)
KW(reg)
KW(wire)

// --- Operators and special characters

Token coma(",", true);
Token lPar("(", true);
Token rPar(")", true);
Token lBracket("[", true);
Token rBracket("]", true);
Token colon(":", true);
Token eol(";", true);
Token ws(" ",  true); 