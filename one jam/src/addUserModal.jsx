import React, {useState} from 'react';
import Modal from 'react-modal';
import Button from '@material-ui/core/Button';
import FormControl from '@material-ui/core/FormControl';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';


function ModalInFunctionalComponent ({HandleLoginSubmit}) {


	let [name, setName] = useState("");
	let [password, setPassword] = useState("");


	function handleSubmit(event) {
		setModalIsOpenToFalse()
		event.preventDefault();
		HandleLoginSubmit(name, password);
		
	}



    const [modalIsOpen,setModalIsOpen] = useState(false);

    const setModalIsOpenToTrue =()=>{
        setModalIsOpen(true)
    }

    const setModalIsOpenToFalse =()=>{
        setModalIsOpen(false)
    }

    return(
        <>
		<button
		onClick={setModalIsOpenToTrue}
		style={{
			backgroundColor:"green",
      width:"80px",
			borderRadius: "4px",
			color: "white",
			border: "none",
			padding:"5px",
      marginBottom: "10px",

		}}
		>Add</button>

            <Modal
			style={{
				overlay: {
					position: 'fixed',
					top: 0,
					left: 0,
					right: 0,
					bottom: 0,
					backgroundColor: 'rgba(255, 255, 255, 0.75)'
				  },
				  content: {
					position: 'absolute',
					top: '45%',
					left: '45%',
					right: '40px',
					bottom: '40px',
					border: '1px solid #ccc',
					background: '#fff',
					overflow: 'auto',
					WebkitOverflowScrolling: 'touch',
					borderRadius: '4px',
					outline: 'none',
					padding: '20px',
					width: "200px",
					height: "210px"

				  }

				
			}}
			
			isOpen={modalIsOpen}>
				<button onClick={setModalIsOpenToFalse}>x</button>

<form className="form" onSubmit={handleSubmit}>
					<FormControl margin="normal" required fullWidth>
						<InputLabel htmlFor="text">Name</InputLabel>
						<Input id="name" name="name"  value={name} autoFocus onChange={(e) => setName(e.target.value.trim())} />
					</FormControl>
					<FormControl margin="normal" required fullWidth>
						<InputLabel htmlFor="password">Port</InputLabel>
						<Input name="password" type="number" id="password"  value={password} onChange={(e) => setPassword(e.target.value.trim())} />
					</FormControl>
					<Button
						type="submit"
						fullWidth
						variant="contained"
						color="primary"
						className="submit"
					>
						Add
					</Button>
				</form>

                
                
            </Modal>
        </>
    )
}
export default ModalInFunctionalComponent;