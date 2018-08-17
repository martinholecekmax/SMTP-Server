--
-- Database: `smtp`
--

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `id` int(11) NOT NULL,
  `subject` varchar(256) DEFAULT NULL,
  `mail_from` varchar(256) NOT NULL,
  `rcpt_to` varchar(25600) NOT NULL,
  `date` date NOT NULL,
  `mime` longtext,
  `body` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `messages`
--

INSERT INTO `messages` (`id`, `subject`, `mail_from`, `rcpt_to`, `date`, `mime`, `body`) VALUES
(31, ' Assigment', 'martin@google.com', 'donny@yahoo.com, bobby@bbc.com', '2017-11-23', NULL, 'Hello world, this is message\r\n');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(64) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `domain` varchar(64) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `mailgroup` varchar(256) DEFAULT NULL,
  `password` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `domain`, `mailgroup`, `password`) VALUES
(1, 'Max', 'centrum.cz', 'people', 'b7becc4f92c747609d4f4bea02ef040a066a160877e086fda50e373069a0187b'),
(5, 'Bill', 'google.com', 'boos', 'b7becc4f92c747609d4f4bea02ef040a066a160877e086fda50e373069a0187b'),
(6, 'Martin', 'derby.ac.uk', 'people', 'b7becc4f92c747609d4f4bea02ef040a066a160877e086fda50e373069a0187b'),
(8, 'John', 'bbc.com', 'people', 'b7becc4f92c747609d4f4bea02ef040a066a160877e086fda50e373069a0187b');

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for table `messages`
--
ALTER TABLE `messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=95;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

