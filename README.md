# cchess
Chinese Chess（象棋）engine with AI

![Game](src/images/game.png?raw=true "Game in progress")

Features:
- Full GUI (requires JavaFX)
  - Chess board
  - Game status (move and check notification)
  - Captured pieces
  - Move log
- Customisable players
  - Human vs Human
  - Human vs AI 
  - AI vs AI
- AI
  - Adjustable search depth and time
  - Move banning
  - Move randomisation
- Others
  - Undo last move/turn
  - Highlight player's legal moves
  - Highlight opponent's last move
  - Save/load game
  - Watch replay
  - Flip board direction
  
AI details:
  - Basic board evaluation
  - Move book (first 2 moves)
  - MiniMax search algorithm
    - Iterative deepening
    - Aspiration windows
    - Principal variation search
    - Alpha-beta pruning
    - Adaptive null move pruning
    - Quiescence search
    - Transposition table with Zobrist hashing
    - Move sorting