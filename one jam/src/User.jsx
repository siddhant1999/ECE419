import {useState } from 'react';

function User(props) {

	const [style, setStyle] = useState({});

	function remove(){
		setStyle({display:"none"})

	}

return (	
<div style={style}>
	<div className="user-info"
	style={{dispaly:"flex",
	flexDirection:"row",
	}}
	>
		<div style={{display:"inline"}}><strong>{props.user_name} : {props.user_port}</strong></div>
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

		onClick={remove}
		>Remove</button>


	</div>
</div>
);

}


export default User;