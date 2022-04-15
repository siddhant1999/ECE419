import './App.css';
import Player from './Player';
import User from './User';
import ModalInFunctionalComponent from './addUserModal'
import { useState, useEffect } from 'react';
import SelectInput from '@material-ui/core/Select/SelectInput';

// let port = 49999;

const data = {

	"Andre":[{
		"name": "Somebody Else",
		"artist": "The 1975",
		"uri": "5hc71nKsUgtwQ3z52KEKQk"
  },
  {
		"name": "Undress Rehearsal",
		"artist": "Timeflies",
		"uri": "1aPqexHVW20OTamBHdWWVL"
  },
  {
		"name": "Daft Pretty Boys",
		"artist": "Bad Suns",
		"uri": "41d2Q6DHcM20OdzynkRtvf"
  }
],
	"Sidd": [
		{
			"name": "Moonwalk",
			"artist": "Rexx Life Raj",
			"uri": "0bJQ2EJndGgy6gN63wSHty"
	  },
	  {
			"name": "Youth",
			"artist": "Glass Animals",
			"uri": "1LPGwuFgIzbJoShfDdw7MY"
	  },
	  {
			"name": "White Ferrari",
			"artist": "Frank Ocean",
			"uri": "2LMkwUfqC6S6s6qDVlEuzV"
	  },
	],

	"Elisa" : [
		{
			"name": "Golden Dandelions",
			"artist": "Barns Courtney",
			"uri": "39mypRmMWQJpVdCsWNxT3h"
	  },
	  {
			"name": "INDUSTRY BABY (feat. Jack Harlow)",
			"artist": "Lil Nas X",
			"uri": "27NovPIUIRrOZoCHxABJwK"
	  },
	  {
			"name": "I Got You",
			"artist": "Bebe Rexha",
			"uri": "1FUViuNSldssMIawrOXF2i"
	  },
	  {
			"name": "Believer",
			"artist": "Imagine Dragons",
			"uri": "6VRghJeP6I0w1KxkdWFfIh"
	  },
	  {
			"name": "Love It If We Made It",
			"artist": "The 1975",
			"uri": "6WmIyn2fx1PKQ0XDpYj4VR"
	  }
	],
	"Song" : [
		{
			"name": "Still Here",
			"artist": "Drake",
			"uri": "433P7tDcIAi6NLnf4Sh6tI"
	  },
	  {
			"name": "Young Shahrukh",
			"artist": "Tesher",
			"uri": "42C9YmmOF7PkiHWpulxzcq"
	  },
	  {
			"name": "Despacito - Remix",
			"artist": "Luis Fonsi",
			"uri": "5CtI0qwDJkDQGwXD1H1cLb"
	  },
	  {
			"name": "Symphony (feat. Zara Larsson)",
			"artist": "Clean Bandit",
			"uri": "72gv4zhNvRVdQA0eOenCal"
	  },
	  {
			"name": "American Money",
			"artist": "B\u00d8RNS",
			"uri": "4AewKenHXKBt643p473xCk"
	  },
	  {
			"name": "Ivy",
			"artist": "Frank Ocean",
			"uri": "2ZWlPOoWh0626oTaHrnl2a"
	  },
	  {
			"name": "Tangerine",
			"artist": "Glass Animals",
			"uri": "40rOlDoGejXXF4B0BYsjx8"
	  },
	]
}

function App() {


  
  let [users, setUsers] = useState(new Array());
  let [playlist, setPlaylist] = useState(new Array());

  const AddSong = (item) => {
    setPlaylist(oldArray => [...oldArray, item]);
  }
  const RemoveSong = (item) =>{
    setPlaylist((prevState) =>
    prevState.filter((prevItem) => prevItem !== item)
  );
  }

  const RemoveUser = (item) =>{
    

  let name = item.user_name;

  for (let i = 0; i < data[name].length; i++) {
    RemoveSong(data[name][i])
  }

  // console.log(item)
  // console.log(users)
  // console.log(users[0]===item)

  setUsers((prevState) =>
    prevState.filter((prevItem) => prevItem.user_name !== item.user_name)
  );



  }
  
  function HandleLoginSubmit(name, port ){

    setUsers(oldArray => [...oldArray, {
      user_name:name,
      user_port:port.toString()
    }]);
    console.log(data[name])
    let t = 0;

    for (let i = 0; i < data[name].length; i++) {
      for (let j = 0; j < 123456789; j++) {t+=1}
      AddSong(data[name][i])
    }
    console.log("playlist")
    console.log(playlist)

    
  }


  return (
    <div className="App">
      <h1>One Jam</h1>
      <div className="spotify-container"
      // style={{width:"",paddingLeft: "10%",paddingRight: "10%"}}
      style={{paddingLeft: "10%"}}
      >
        {/* <div className="main-player" style={{width: "300px"}}>
          <Player/>
        </div> */}
        <div className='queue'>
        {playlist && playlist.map(song =>

            <iframe 
            style={{borderRadius:"12px"}}
            src={`https://open.spotify.com/embed/track/`+song.uri+`?utm_source=generator`}
            width="100%"
            height="80px"
            title="player"
            frameBorder="0"
            ></iframe>


           )}

        </div>
      
      </div>
      <div className="user-container"
      style={{width:"80%", margin: "auto" }}
      >

        <h2
        style={{
        }}
        >
          Users
        </h2>

      <ModalInFunctionalComponent HandleLoginSubmit={HandleLoginSubmit} addSong={AddSong}/>
        {users && users.map(user =>
        <div className="user-info"
        style={{dispaly:"flex",
        flexDirection:"row",
        }}
        >
          <div style={{display:"inline"}}><strong>{user.user_name} : {user.user_port}</strong></div>
          <button
          style={{
            backgroundColor:"red",
            marginLeft: "30px",
            borderRadius: "4px",
            marginTop: "20px",
            color: "white",
            border: "none",
            padding:"5px",
        
          }}
      
          onClick={() => RemoveUser({user_name: user.user_name, user_port: user.user_port})}
          >Remove</button>
      
      
        </div>
              
          // <User user_name={user.user_name} user_port={user.user_port} remove={RemoveUser}/>
                  
       )}
      </div>
      <div>
      
      </div>

    </div>
  );
}

export default App;
